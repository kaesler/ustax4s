package org.kae.ustax4s

package forms

final case class ScheduleD(
  longTermCapitalGains: TMoney,
  capitalGainsDistributions: TMoney
) {

  def netLongTermCapitalGains: TMoney =
    longTermCapitalGains + capitalGainsDistributions
}
