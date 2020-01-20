package org.kae.ustax4s

package forms

final case class Schedule1(
  scheduleD: Option[ScheduleD],
  // TODO: may need negative Money type here
  // Line 12:
  businessIncomeOrLoss: TMoney,

  // Line 25:
  healthSavingsAccountDeduction: TMoney,

  // Line 27:
  deductiblePartOfSelfEmploymentTax: TMoney
) {

  // TODO: may need negative Money type here
  def capitalGainOrLoss: TMoney =
    scheduleD.map(_.netLongTermCapitalGains).getOrElse(TMoney.zero)

  def additionalIncome: TMoney = businessIncomeOrLoss + capitalGainOrLoss

  def adjustmentsToIncome: TMoney =
    healthSavingsAccountDeduction + deductiblePartOfSelfEmploymentTax
}
