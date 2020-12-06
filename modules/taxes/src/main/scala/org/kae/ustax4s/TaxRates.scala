package org.kae.ustax4s

import java.time.{LocalDate, Year}
import org.kae.ustax4s.forms.Form1040

/**
  * Note: does not model exemptions that pertained before 2018 and
  * could be re-introduced in 2025
  *
  * @param standardDeduction
  * @param ordinaryIncomeBrackets
  * @param investmentIncomeBrackets
  * @param filingStatus
  */
case class TaxRates(
  standardDeduction: TMoney,
  ordinaryIncomeBrackets: OrdinaryIncomeTaxBrackets,
  investmentIncomeBrackets: InvestmentIncomeTaxBrackets,
  filingStatus: FilingStatus
) {
  // Line 11:
  def taxDueBeforeCredits(
    ordinaryIncome: TMoney,
    investmentIncome: TMoney
  ): TMoney =
    ordinaryIncomeBrackets.taxDue(ordinaryIncome) +
      investmentIncomeBrackets.taxDue(ordinaryIncome, investmentIncome)


  // Line 15:
  def totalTax(form: Form1040): TMoney =
    taxDueBeforeCredits(form.taxableOrdinaryIncome, form.qualifiedInvestmentIncome) +
      form.schedule4.map(_.totalOtherTaxes).getOrElse(TMoney.zero) -
      (form.childTaxCredit + form.schedule3
        .map(_.nonRefundableCredits)
        .getOrElse(TMoney.zero))
}

object TaxRates {
  def of (year: Year, filingStatus: FilingStatus, birthDate: LocalDate): TaxRates =
    TaxRates(
      StandardDeduction.of(year, filingStatus, birthDate),
      OrdinaryIncomeTaxBrackets.of(year, filingStatus),
      InvestmentIncomeTaxBrackets.of(year, filingStatus),
      filingStatus
    )
}
