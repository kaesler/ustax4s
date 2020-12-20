package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.forms.Form1040

/**
  * Simplified interface to 1040 calcs.
  * Assume:
  *   - The only non-SS income is from 401k.
  *   - No capital gains, deductions credits or other complications.
  */
object SimpleTaxInRetirement extends IntMoneySyntax {

  def taxDue(
    year: Year,
    filingStatus: FilingStatus,
    incomeFrom401k: TMoney,
    ss: TMoney
  ): TMoney = {
    val rates = TaxRates.of(year, filingStatus, Kevin.birthDate)
    val taxableSocialSecurity =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus,
        incomeFrom401k,
        ss
      )

    // Note the order here:
    //  1.sum all income with SS.
    //  2. subtract standard deduction.
    val taxableIncome = (incomeFrom401k + taxableSocialSecurity) - rates.standardDeduction

    rates.ordinaryIncomeBrackets.taxDue(taxableIncome).rounded
  }

  def taxDueUsingForm1040(
    year: Year,
    filingStatus: FilingStatus,
    incomeFrom401k: TMoney,
    ss: TMoney
  ): TMoney = {
    val myRates = TaxRates.of(
      year,
      filingStatus,
      Kevin.birthDate
    )

    val form = Form1040(
      filingStatus,
      rates = myRates,
      taxableIras = incomeFrom401k,
      socialSecurityBenefits = ss,
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
      qualifiedDividends = TMoney.zero,
      ordinaryDividends = TMoney.zero
    )
    myRates.totalTax(form).rounded
  }
}
