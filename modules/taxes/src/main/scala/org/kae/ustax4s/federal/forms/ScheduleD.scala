package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.*

final case class ScheduleD(
  // Line 8b
  longTermCapitalGains: TMoney,
  // Line 13
  capitalGainsDistributions: TMoney
):
  def netLongTermCapitalGains: TMoney =
    longTermCapitalGains + capitalGainsDistributions
