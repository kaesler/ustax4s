package org.kae.ustax4s.federal

import java.time.Year
import java.time.LocalDate
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.FilingStatus

trait FederalTaxCalculator:

  def federalTaxResults(
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    itemizedDeductions: Money
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
    ): FederalTaxResults = {
      val ssRelevantOtherIncome = ordinaryIncomeNonSS + qualifiedIncome
      val taxableSocialSecurity =
        TaxableSocialSecurity.taxableSocialSecurityBenefits(
          filingStatus = filingStatus,
          socialSecurityBenefits = socSec,
          ssRelevantOtherIncome
        )

      val taxableOrdinaryIncome = (taxableSocialSecurity + ordinaryIncomeNonSS) subp
        regime.netDeduction(year, filingStatus, birthDate, personalExemptions, itemizedDeductions)

      val taxOnOrdinaryIncome = regime
        .ordinaryIncomeBrackets(year, filingStatus)
        .taxDue(taxableOrdinaryIncome)
      val taxOnQualifiedIncome = regime
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
    }
  }
end FederalTaxCalculator
