package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.Money

final case class Schedule1(
  scheduleD: Option[ScheduleD],
  // TODO: may need negative Money type here
  // Line 12:
  businessIncomeOrLoss: Money,
  // Line 25:
  healthSavingsAccountDeduction: Money,
  // Line 27:
  deductiblePartOfSelfEmploymentTax: Money
):
  // TODO: may need negative Money type here
  def capitalGainOrLoss: Money =
    scheduleD.map(_.netLongTermCapitalGains).getOrElse(Money.zero)

  def additionalIncome: Money = businessIncomeOrLoss + capitalGainOrLoss

  def adjustmentsToIncome: Money =
    healthSavingsAccountDeduction + deductiblePartOfSelfEmploymentTax
