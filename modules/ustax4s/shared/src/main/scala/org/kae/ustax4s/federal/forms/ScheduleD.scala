package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.TaxableIncome

final case class ScheduleD(
  // Line 8b
  longTermCapitalGains: TaxableIncome,
  // Line 13
  capitalGainsDistributions: TaxableIncome
):
  def netLongTermCapitalGains: TaxableIncome =
    longTermCapitalGains + capitalGainsDistributions
end ScheduleD
