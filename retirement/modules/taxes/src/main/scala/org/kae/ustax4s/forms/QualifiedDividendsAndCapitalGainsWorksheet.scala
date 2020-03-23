package org.kae.ustax4s

package forms

final case class QualifiedDividendsAndCapitalGainsWorksheet(
  taxBrackets: OrdinaryIncomeTaxBrackets,
  cgTaxBrackets: InvestmentIncomeTaxBrackets
) {

  // TODO: how to express this in an LP model?
  //  - compute tax on regular and investments separately
  //  - adjust brackets for non-investments by amount receiving preferential treatment?

  // Line 4
  def preferentiallyTaxedGains(form1040: Form1040): TMoney =
    form1040.qualifiedDividends +
      form1040.scheduleD
        .map(_.netLongTermCapitalGains)
        .getOrElse(TMoney.zero)

  def taxOnPTGains: TMoney = ???

  def taxOnNonGains: TMoney = ???

  // Line 27
  def taxOnAllTaxableIncome: TMoney =
    // Lesser of:
    //   - preferential rates applied
    //   - no preferential rates applied
    ???
}