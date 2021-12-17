package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.*

final case class Schedule1(
  scheduleD: Option[ScheduleD],
  // TODO: may need negative Money type here
  // Better yet Income | Loss
  // Line 12:
  businessIncomeOrLoss: Income,
  // Line 25:
  healthSavingsAccountDeduction: Deduction,
  // Line 27:
  deductiblePartOfSelfEmploymentTax: Deduction
):
  // TODO: may need negative Money type here
  // or Income | Loss
  def capitalGainOrLoss: Income =
    scheduleD.map(_.netLongTermCapitalGains).getOrElse(Income.zero)

  def additionalIncome: Income = businessIncomeOrLoss + capitalGainOrLoss

  def adjustmentsToIncome: Deduction =
    healthSavingsAccountDeduction + deductiblePartOfSelfEmploymentTax
