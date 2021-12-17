package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.*

final case class ScheduleD(
  // Line 8b
  longTermCapitalGains: Income,
  // Line 13
  capitalGainsDistributions: Income
):
  def netLongTermCapitalGains: Income =
    longTermCapitalGains + capitalGainsDistributions
