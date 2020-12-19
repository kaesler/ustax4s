package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.forms.Form1040

/**
  * Simplified interface to 1040 calcs.
  * Only non-SS income is from 401k.
  */
object SimpleTaxInRetirement extends IntMoneySyntax {

  /**
    * Simplified calc: Directly apply tax brackets and std deduction.
    * @param year the [[Year]]
    * @param filingStatus the [[FilingStatus]]
    * @param incomeFrom401k income from 401k
    * @return
    */
  def ordinaryIncomeTaxDueNoSS(
    year: Year,
    filingStatus: FilingStatus,
    incomeFrom401k: TMoney
  ): TMoney = {
    val rates = TaxRates.of(year, filingStatus, Kevin.birthDate)
    val taxableOrdinaryIncome = incomeFrom401k - rates.standardDeduction
    rates.ordinaryIncomeBrackets.taxDue(taxableOrdinaryIncome)
  }

  def taxDueWithSS(
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
