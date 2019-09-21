package org.kae.ustax4s
package forms

final case class Form1040(
  schedule1: Schedule1,
  schedule3: Schedule3,
  schedule4: Schedule4,
  childTaxCredit: TMoney = TMoney.u(2_000),
  wages: TMoney,
  taxableInterest: TMoney,
  ordinaryDividends: TMoney,
  taxableIras: TMoney,
  taxableSocialSecurityBenefits: TMoney,
  rates: TaxRates
) {

  def totalIncome: TMoney =
    TMoney.sum(
      wages,
      taxableInterest,
      ordinaryDividends,
      taxableIras,
      taxableSocialSecurityBenefits,
      schedule1.additionalIncome
    )

  def adjustedGrossIncome: TMoney =
    totalIncome subtract schedule1.adjustmentsToIncome

  def taxableIncome: TMoney =
    adjustedGrossIncome subtract rates.standardDeduction

  def tax: TMoney = rates.brackets.taxDue(taxableIncome)

  def totalTax: TMoney =
    (tax + schedule4.totalOtherTaxes) subtract
      TMoney.sum(childTaxCredit, schedule3.nonRefundableCredits)
}
