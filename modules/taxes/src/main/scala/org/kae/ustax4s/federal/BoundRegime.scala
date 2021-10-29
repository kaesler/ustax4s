package org.kae.ustax4s.federal

import java.time.{LocalDate, Year}
import org.kae.ustax4s.{FilingStatus, Inflation}
import org.kae.ustax4s.money.Money

trait BoundRegime(
  regime: Regime,
  year: Year,
  filingStatus: FilingStatus,
  birthDate: LocalDate,
  personalExemptions: Int
):
  def standardDeduction: Money
  def personalExemptionDeduction: Money
  def ordinaryIncomeBrackets: OrdinaryIncomeBrackets
  def qualifiedIncomeBrackets: QualifiedIncomeBrackets

  def netDeduction(itemizedDeductions: Money): Money =
    Money.max(
      standardDeduction,
      personalExemptionDeduction + itemizedDeductions
    )

  def calculator: FederalTaxCalculator =
    (
      socSec: Money,
      ordinaryIncomeNonSS: Money,
      qualifiedIncome: Money,
      itemizedDeductions: Money
    ) => {

      val ssRelevantOtherIncome = ordinaryIncomeNonSS + qualifiedIncome

      // Note: this does not currently get adjusted for inflation.
      val taxableSocialSecurity =
        TaxableSocialSecurity.taxableSocialSecurityBenefits(
          filingStatus = filingStatus,
          socialSecurityBenefits = socSec,
          ssRelevantOtherIncome
        )

      val taxableOrdinaryIncome =
        (taxableSocialSecurity + ordinaryIncomeNonSS) subp
          netDeduction(itemizedDeductions)

      val taxOnOrdinaryIncome = ordinaryIncomeBrackets.taxDue(taxableOrdinaryIncome)

      val taxOnQualifiedIncome = qualifiedIncomeBrackets.taxDueFunctionally(
        taxableOrdinaryIncome,
        qualifiedIncome
      )
      FederalTaxResults(
        ssRelevantOtherIncome,
        taxableSocialSecurity,
        personalExemptionDeduction,
        standardDeduction,
        netDeduction(itemizedDeductions),
        taxableOrdinaryIncome,
        taxOnOrdinaryIncome,
        taxOnQualifiedIncome
      )
    }

  // Create a new Regime that behaves like the original but with appropriate
  // adjustments for inflation.
  def inflatedBy(inflation: Inflation): BoundRegime =
    val base = this
    new BoundRegime(regime, year, filingStatus, birthDate, personalExemptions) {

//      override val name =
//        s"${base.name}-inflatedTo-${inflation.targetFutureYear.getValue}"

      override val standardDeduction: Money =
        base.standardDeduction mul inflation.factor(year)

      override val personalExemptionDeduction: Money =
        base.personalExemptionDeduction mul inflation.factor(year)

      override val ordinaryIncomeBrackets: OrdinaryIncomeBrackets =
        base.ordinaryIncomeBrackets.inflatedBy(inflation.factor(year))

      override val qualifiedIncomeBrackets: QualifiedIncomeBrackets =
        base.qualifiedIncomeBrackets.inflatedBy(inflation.factor(year))
    }

end BoundRegime

object BoundRegime:

  def create(
    regime: Regime,
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int
  ): BoundRegime =
    new BoundRegime(regime, year, filingStatus, birthDate, personalExemptions) {

      override def standardDeduction: Money =
        regime.standardDeduction(year, filingStatus, birthDate)

      override def personalExemptionDeduction: Money =
        regime.personalExemptionDeduction(year, personalExemptions)

      override def ordinaryIncomeBrackets: OrdinaryIncomeBrackets =
        regime.ordinaryIncomeBrackets(year, filingStatus)

      override def qualifiedIncomeBrackets: QualifiedIncomeBrackets =
        regime.qualifiedIncomeBrackets(year, filingStatus)
    }
