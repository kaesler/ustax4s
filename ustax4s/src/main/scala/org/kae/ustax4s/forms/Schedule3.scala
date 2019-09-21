package org.kae.ustax4s
package forms

final case class Schedule3(
  foreignTaxCredit: TMoney
) {
  def nonRefundableCredits: TMoney = foreignTaxCredit
}
