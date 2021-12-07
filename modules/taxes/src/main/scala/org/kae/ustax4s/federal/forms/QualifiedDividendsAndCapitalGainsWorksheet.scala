package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.federal.{OrdinaryIncomeBrackets, QualifiedIncomeBrackets}
import org.kae.ustax4s.money.Money

final case class QualifiedDividendsAndCapitalGainsWorksheet(
  ordinaryBrackets: OrdinaryIncomeBrackets,
  qualifiedBrackets: QualifiedIncomeBrackets
):
  // TODO: how to express this in an LP model?
  //  - compute tax on regular and investments separately
  //  - adjust brackets for non-investments by amount receiving preferential treatment?

  // Line 4
  def preferentiallyTaxedGains(form1040: Form1040): Money =
    form1040.qualifiedDividends +
      form1040.scheduleD
        .map(_.netLongTermCapitalGains)
        .getOrElse(0)

  def taxOnPTGains: Money = ???

  def taxOnNonGains: Money = ???

  // Line 27
  def taxOnAllTaxableIncome: Money =
    // Lesser of:
    //   - preferential rates applied
    //   - no preferential rates applied
    ???
