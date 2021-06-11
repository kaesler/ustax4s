package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.Money

final case class Schedule3(
  // Line 48:
  foreignTaxCredit: Money
):
  def nonRefundableCredits: Money = foreignTaxCredit
