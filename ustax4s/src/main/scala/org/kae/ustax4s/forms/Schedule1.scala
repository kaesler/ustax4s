package org.kae.ustax4s

package forms

import org.kae.ustax4s.TMoney

final case class Schedule1(
  scheduleD: Option[ScheduleD],
  // TODO: may need negative Money type here
  businessIncomeOrLoss: TMoney,
  healthSavingsAccountDeduction: TMoney,
  deductiblePartOfSelfEmploymentTax: TMoney
) {

  // TODO: may need negative Money type here
  def capitalGainOrLoss: TMoney =
    scheduleD.map(_.netLongTermCapitalGains).getOrElse(TMoney.zero)

  def additionalIncome: TMoney = businessIncomeOrLoss + capitalGainOrLoss

  def adjustmentsToIncome: TMoney =
    healthSavingsAccountDeduction + deductiblePartOfSelfEmploymentTax
}
