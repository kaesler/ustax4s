package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.*

final case class Schedule3(
  // Line 48:
  foreignTaxCredit: TaxCredit
):
  def nonRefundableCredits: TaxCredit = foreignTaxCredit
end Schedule3
