package org.kae.ustax4s.federal

import java.time.Year
import java.time.LocalDate
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.{FilingStatus, Inflation}

trait FederalTaxCalculator:

  // Note: We could have just used cyrrying, i.e.
  //   trait FederalTaxCalculator extends ((Money .. Money) => FederalTaxResults
  // but it is more ergonomic to allow named arguments.

  // Compute tax results for the given inputs.
  // By default, un-inflated tax brackets, deductions and so ond are used.
  def federalTaxResults(
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    itemizedDeductions: Money
  )(
    inflation: Option[Inflation] = None
  ): FederalTaxResults

end FederalTaxCalculator

object FederalTaxCalculator:

  def create(
    baseRegime: Regime,
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    personalExemptions: Int
  ): FederalTaxCalculator = new {

    override def federalTaxResults(
      socSec: Money,
      ordinaryIncomeNonSS: Money,
      qualifiedIncome: Money,
      itemizedDeductions: Money
    )(
      inflation: Option[Inflation]
    ): FederalTaxResults =

      val effectiveRegime       = inflation.fold(baseRegime)(baseRegime.inflatedBy)
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
          effectiveRegime.netDeduction(
            year,
            filingStatus,
            birthDate,
            personalExemptions,
            itemizedDeductions
          )

      val taxOnOrdinaryIncome = effectiveRegime
        .ordinaryIncomeBrackets(year, filingStatus)
        .taxDue(taxableOrdinaryIncome)

      val taxOnQualifiedIncome = effectiveRegime
        .qualifiedIncomeBrackets(year, filingStatus)
        .taxDueFunctionally(
          taxableOrdinaryIncome,
          qualifiedIncome
        )
      FederalTaxResults(
        ssRelevantOtherIncome,
        taxableSocialSecurity,
        effectiveRegime.personalExemptionDeduction(year, personalExemptions),
        effectiveRegime.standardDeduction(year, filingStatus, birthDate),
        effectiveRegime
          .netDeduction(year, filingStatus, birthDate, personalExemptions, itemizedDeductions),
        taxableOrdinaryIncome,
        taxOnOrdinaryIncome,
        taxOnQualifiedIncome
      )
    end federalTaxResults
  }

end FederalTaxCalculator
