package org.kae.ustax4s

package forms

import org.kae.ustax4s.TMoney

final case class Schedule1(
  // TODO: may need negative Money type here
  businessIncomeOrLoss: TMoney,
  capitalGainOrLoss: TMoney,
  healthSavingsAccountDeduction: TMoney,
  deductiblePartOfSelfEmploymentTax: TMoney
) {

  def additionalIncome: TMoney = businessIncomeOrLoss + capitalGainOrLoss

  def adjustmentsToIncome: TMoney =
    healthSavingsAccountDeduction + deductiblePartOfSelfEmploymentTax
}
