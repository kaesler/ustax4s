package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.Money

final case class ScheduleD(
  // Line 8b
  longTermCapitalGains: Money,
  // Line 13
  capitalGainsDistributions: Money
):
  def netLongTermCapitalGains: Money =
    longTermCapitalGains + capitalGainsDistributions
