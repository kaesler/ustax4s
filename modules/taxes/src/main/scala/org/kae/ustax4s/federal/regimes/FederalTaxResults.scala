package org.kae.ustax4s.federal.regimes

import org.kae.ustax4s.money.Money

final case class FederalTaxResults(
  ssRelevantOtherIncome: Money,
  taxableSocialSecurity: Money,
  personalExceptionDeduction: Money,
  standardDeduction: Money,
  netDeduction: Money,
  taxableOrdinaryIncome: Money,
  taxOnOrdinaryIncome: Money,
  taxOnQualifiedIncome: Money
):
  def taxDue: Money = taxOnOrdinaryIncome + taxOnQualifiedIncome
