package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.forms.Form1040

/**
 * Simplified interface to 1040 calcs.
 * 2021 rates assumed.
 * Only non-SS income is from 401k.
 */
object SimpleTaxInRetirement extends IntMoneySyntax {

  def taxDueWithSS(
    filingStatus: FilingStatus,
    incomeFrom401k: TMoney,
    ss: TMoney
  ): TMoney = {
    val year = Year.of(2021)
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
