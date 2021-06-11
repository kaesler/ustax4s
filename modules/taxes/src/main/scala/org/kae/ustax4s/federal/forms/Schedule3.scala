package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.moneyold.*

final case class Schedule3(
  // Line 48:
  foreignTaxCredit: TMoney
):
  def nonRefundableCredits: TMoney = foreignTaxCredit
