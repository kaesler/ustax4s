package org.kae.ustax4s.federal

import java.time.Year
import java.time.LocalDate
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.FilingStatus

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
    regime: Regime,
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
      val inflationFactor = inflation.map(_.factor).getOrElse(1.0)

      val ssRelevantOtherIncome = ordinaryIncomeNonSS + qualifiedIncome
      val taxableSocialSecurity =
        TaxableSocialSecurity.taxableSocialSecurityBenefits(
          filingStatus = filingStatus,
          socialSecurityBenefits = socSec,
          ssRelevantOtherIncome
        )

      val taxableOrdinaryIncome =
        (taxableSocialSecurity + ordinaryIncomeNonSS) subp
          // TODO: would need inject inflation factor here.
          regime.netDeduction(year, filingStatus, birthDate, personalExemptions, itemizedDeductions)

      val taxOnOrdinaryIncome = regime
        // TODO: would need inject inflation factor here.
        .ordinaryIncomeBrackets(year, filingStatus)
        .taxDue(taxableOrdinaryIncome)

      val taxOnQualifiedIncome = regime
        // TODO: would need inject inflation factor here.
        .qualifiedIncomeBrackets(year, filingStatus)
        .taxDueFunctionally(
          taxableOrdinaryIncome,
          qualifiedIncome
        )
      FederalTaxResults(
        ssRelevantOtherIncome,
        taxableSocialSecurity,
        regime.personalExemptionDeduction(year, personalExemptions),
        regime.standardDeduction(year, filingStatus, birthDate),
        regime.netDeduction(year, filingStatus, birthDate, personalExemptions, itemizedDeductions),
        taxableOrdinaryIncome,
        taxOnOrdinaryIncome,
        taxOnQualifiedIncome
      )
    end federalTaxResults
  }

end FederalTaxCalculator
