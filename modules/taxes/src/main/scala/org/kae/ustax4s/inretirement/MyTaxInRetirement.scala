package org.kae.ustax4s
package inretirement

import java.time.Year
import org.kae.ustax4s.forms.Form1040

object MyTaxInRetirement extends IntMoneySyntax {

  // TODO: add:
  //   - state tax

  def federalTaxDue(
    year: Year,
    socSec: TMoney,
    ordinaryIncomeNonSS: TMoney,
    qualifiedIncome: TMoney
  ): TMoney =
    TaxInRetirement.federalTaxDue(
      year,
      Kevin.birthDate,
      Kevin.filingStatus(year),
      socSec,
      ordinaryIncomeNonSS,
      qualifiedIncome
    )

  def federalTaxDueUsingForm1040(
    year: Year,
    socSec: TMoney,
    ordinaryIncomeNonSS: TMoney,
    qualifiedDividends: TMoney,
    verbose: Boolean
  ): TMoney = {
    val filingStatus = Kevin.filingStatus(year)
    val myRates = TaxRates.of(
      year,
      filingStatus,
      Kevin.birthDate
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
