package org.kae.ustax4s

package forms

final case class Form1040(
  schedule1: Option[Schedule1],
  schedule3: Option[Schedule3],
  schedule4: Option[Schedule4],
  childTaxCredit: TMoney = TMoney.u(2000),
  wages: TMoney,
  taxableInterest: TMoney,
  qualifiedDividends: TMoney,
  ordinaryDividends: TMoney,
  taxableIras: TMoney,
  taxableSocialSecurityBenefits: TMoney,
  rates: TaxRates
) {

  def scheduleD: Option[ScheduleD] = schedule1.flatMap(_.scheduleD)

  def totalIncome: TMoney =
    TMoney.sum(
      wages,
      taxableInterest,
      ordinaryDividends,
      taxableIras,
      taxableSocialSecurityBenefits,
      schedule1.map(_.additionalIncome).getOrElse(TMoney.zero)
    )

  def adjustedGrossIncome: TMoney =
    totalIncome - schedule1.map(_.adjustmentsToIncome).getOrElse(TMoney.zero)

  def taxableIncome: TMoney =
    adjustedGrossIncome - rates.standardDeduction

  def tax: TMoney = rates.brackets.taxDue(taxableIncome)

  def totalTax: TMoney =
    tax + schedule4.map(_.totalOtherTaxes).getOrElse(TMoney.zero) -
      (childTaxCredit + schedule3
        .map(_.nonRefundableCredits)
        .getOrElse(TMoney.zero))
}
