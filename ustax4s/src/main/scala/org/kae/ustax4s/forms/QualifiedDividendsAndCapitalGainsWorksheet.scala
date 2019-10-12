package org.kae.ustax4s

package forms

final case class QualifiedDividendsAndCapitalGainsWorksheet(
  form1040: Form1040,
  cgTaxBrackets: CGTaxBrackets
) {

  // TODO: Schedule D vs 1 ?

  def preferentiallyTaxedGains: TMoney =
    form1040.qualifiedDividends + form1040.scheduleD
      .map(_.netLongTermCapitalGains)
      .getOrElse(TMoney.zero)

  def taxOnGains: TMoney = ???

  def taxOnNonGains: TMoney = ???

  def taxOnAllTaxableIncome: TMoney = ???
}
