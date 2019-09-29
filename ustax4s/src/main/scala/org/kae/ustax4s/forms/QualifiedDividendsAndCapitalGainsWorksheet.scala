package org.kae.ustax4s
package forms

final case class QualifiedDividendsAndCapitalGainsWorksheet(
  form1040: Form1040,
  cgTaxBrackets: CGTaxBrackets
) {
  def preferentiallyTaxedGains: TMoney = ???

  def taxOnGains: TMoney = ???

  def taxOnNonGains: TMoney = ???

  def taxOnAllTaxableIncome: TMoney = ???
}
