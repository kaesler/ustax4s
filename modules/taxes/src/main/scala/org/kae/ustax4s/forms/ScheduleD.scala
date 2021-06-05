package org.kae.ustax4s

package forms

final case class ScheduleD(
  // Line 8b
  longTermCapitalGains: TMoney,
  // Line 13
  capitalGainsDistributions: TMoney
):
  def netLongTermCapitalGains: TMoney =
    longTermCapitalGains + capitalGainsDistributions
