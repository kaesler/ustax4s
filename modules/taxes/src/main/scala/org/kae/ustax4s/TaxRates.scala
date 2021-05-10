package org.kae.ustax4s

import java.time.{LocalDate, Year}
import org.kae.ustax4s.forms.Form1040

// Note: does not model exemptions that pertained before 2018 and could
// be re-introduced in 2025.
case class TaxRates(
  standardDeduction: TMoney,
  ordinaryIncomeBrackets: OrdinaryIncomeBrackets,
  qualifiedIncomeBrackets: QualifiedIncomeBrackets,
  filingStatus: FilingStatus
) {

  // Line 11:
  def taxDueBeforeCredits(
    ordinaryIncome: TMoney,
    qualifiedIncome: TMoney
  ): TMoney =
    ordinaryIncomeBrackets.taxDue(ordinaryIncome) +
      qualifiedIncomeBrackets.taxDueFunctionally(
        ordinaryIncome,
        qualifiedIncome
      )

  // Line 15:
  def totalTax(form: Form1040): TMoney =
    taxDueBeforeCredits(
      form.taxableOrdinaryIncome,
      form.qualifiedIncome
    ) +
      form.schedule4.map(_.totalOtherTaxes).getOrElse(TMoney.zero) -
      (form.childTaxCredit + form.schedule3
        .map(_.nonRefundableCredits)
        .getOrElse(TMoney.zero))
}

object TaxRates {

  def of(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate
  ): TaxRates =
    TaxRates(
      StandardDeduction.of(year, filingStatus, birthDate),
      OrdinaryIncomeBrackets.of(year, filingStatus),
      QualifiedIncomeBrackets.of(year, filingStatus),
      filingStatus
    )
}
