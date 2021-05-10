package org.kae.ustax4s
package inretirement

import java.time.{LocalDate, Year}
import org.kae.ustax4s.forms.Form1040

/** Simplified interface to 1040 calculations. Assume: No deductions credits or other complications.
  */
object TaxInRetirement extends IntMoneySyntax {

  // TODO: add:
  //   - unqualified dividends
  //   - earned income
  //   - state tax
  def federalTaxDue(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: TMoney,
    ordinaryIncomeNonSS: TMoney,
    qualifiedIncome: TMoney
  ): TMoney = {
    val rates = TaxRates.of(year, filingStatus, birthDate)
    val taxableSocialSecurity =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = filingStatus,
        socialSecurityBenefits = socSec,
        ssRelevantOtherIncome = ordinaryIncomeNonSS + qualifiedIncome
      )
    val taxableOrdinaryIncome = (taxableSocialSecurity + ordinaryIncomeNonSS) -
      rates.standardDeduction
    val taxOnOrdinaryIncome =
      rates.ordinaryIncomeBrackets.taxDue(taxableOrdinaryIncome)
    val taxOnQualifiedIncome = rates.qualifiedIncomeBrackets.taxDueFunctionally(
      taxableOrdinaryIncome,
      qualifiedIncome
    )
    (taxOnQualifiedIncome + taxOnOrdinaryIncome).rounded
  }

  // TODO: do I need this?
  def federalTaxDueNoQualifiedInvestments(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: TMoney,
    ordinaryIncomeNonSS: TMoney
  ): TMoney = {
    val rates = TaxRates.of(year, filingStatus, birthDate)
    val taxableSocialSecurity =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = filingStatus,
        ssRelevantOtherIncome = ordinaryIncomeNonSS,
        socialSecurityBenefits = socSec
      )

    // Note the order here:
    //  1.sum all income with SS.
    //  2. subtract standard deduction.
    val taxableIncome = (ordinaryIncomeNonSS + taxableSocialSecurity) -
      rates.standardDeduction

    rates.ordinaryIncomeBrackets.taxDue(taxableIncome).rounded
  }

  def federalTaxDueUsingForm1040(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: TMoney,
    ordinaryIncomeNonSS: TMoney,
    qualifiedDividends: TMoney,
    verbose: Boolean
  ): TMoney = {
    val myRates = TaxRates.of(
      year,
      filingStatus,
      birthDate
    )

    val form = Form1040(
      filingStatus,
      rates = myRates,
      taxableIraDistributions = ordinaryIncomeNonSS,
      socialSecurityBenefits = socSec,
      // The rest not applicable in retirement.
      standardDeduction = myRates.standardDeduction,
      schedule1 = None,
      schedule3 = None,
      schedule4 = None,
      schedule5 = None,
      childTaxCredit = TMoney.zero,
      wages = TMoney.zero,
      taxExemptInterest = TMoney.zero,
      taxableInterest = TMoney.zero,
      qualifiedDividends = qualifiedDividends,
      ordinaryDividends = qualifiedDividends
    )
    if (verbose) {
      println(form.showValues)
    }

    myRates.totalTax(form).rounded
  }

  def stateTaxDue(
    year: Year,
    // Excludes SocSec. So it is
    //  - earned wages
    //  - interest
    //  - dividends
    //  - capital gains
    taxableIncome: TMoney
  ): TMoney =
    StateTaxMA.taxDue(
      year,
      Kevin.filingStatus(year),
      Kevin.birthDate,
      Kevin.numberOfDependents(year)
    )(taxableIncome)
}
