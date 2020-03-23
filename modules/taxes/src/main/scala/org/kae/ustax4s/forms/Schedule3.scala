package org.kae.ustax4s
package forms

final case class Schedule3(
  // Line 48:
  foreignTaxCredit: TMoney
) {
  def nonRefundableCredits: TMoney = foreignTaxCredit
}
