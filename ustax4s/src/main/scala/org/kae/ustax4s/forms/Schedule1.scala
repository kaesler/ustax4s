package org.kae.ustax4s.forms

import org.kae.ustax4s.TMoney

final case class Schedule1(
  // TODO: may need negative Money type here
  businessIncomeOrLoss: TMoney,
  capitalGainOrLoss: TMoney,
  healthSavingsAccountDeduction: TMoney,
  deductiblePartOfSelfEmploymentTax: TMoney
) {

  def additionalIncome: TMoney =
    TMoney.sum(businessIncomeOrLoss, capitalGainOrLoss)

  def adjustmentsToIncome: TMoney =
    TMoney.sum(healthSavingsAccountDeduction, deductiblePartOfSelfEmploymentTax)
}
