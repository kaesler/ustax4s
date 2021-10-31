package org.kae.ustax4s.federal

import java.time.{LocalDate, Year}
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.{FilingStatus, InflationEstimate}

trait BoundRegime(
  regime: Regime,
  val year: Year,
  filingStatus: FilingStatus,
  birthDate: LocalDate,
  personalExemptions: Int
):
  def standardDeduction: Money
  def personalExemptionDeduction: Money
  def ordinaryIncomeBrackets: OrdinaryIncomeBrackets
  def qualifiedIncomeBrackets: QualifiedIncomeBrackets

  def name: String = regime.name

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

  // Create a new BoundRegime that behaves like the original but with
  // appropriate adjustments for inflation estimate.
  def futureEstimated(estimate: InflationEstimate): BoundRegime =
    val base = this
    new BoundRegime(
      regime,
      estimate.targetFutureYear,
      filingStatus,
      birthDate,
      personalExemptions
    ) {

      // TODO: restrict what is legal.
      import math.Ordered.orderingToOrdered
      require(estimate.targetFutureYear > base.year)
      regime.failIfInvalid(estimate.targetFutureYear)

      override val name =
        s"${base.name}-estimatedFor-${estimate.targetFutureYear.getValue}"

      override val standardDeduction: Money =
        base.standardDeduction mul estimate.factor(base.year)

      override val personalExemptionDeduction: Money =
        base.personalExemptionDeduction mul estimate.factor(base.year)

      override val ordinaryIncomeBrackets: OrdinaryIncomeBrackets =
        base.ordinaryIncomeBrackets.inflatedBy(estimate.factor(base.year))

      override val qualifiedIncomeBrackets: QualifiedIncomeBrackets =
        base.qualifiedIncomeBrackets.inflatedBy(estimate.factor(base.year))
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
        regime.standardDeduction(this.year, filingStatus, birthDate)

      override def personalExemptionDeduction: Money =
        regime.personalExemptionDeduction(this.year, personalExemptions)

      override def ordinaryIncomeBrackets: OrdinaryIncomeBrackets =
        regime.ordinaryIncomeBrackets(this.year, filingStatus)

      override def qualifiedIncomeBrackets: QualifiedIncomeBrackets =
        regime.qualifiedIncomeBrackets(this.year, filingStatus)
    }