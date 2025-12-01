package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.federal.{OrdinaryRateFunction, QualifiedRateFunction}
import org.kae.ustax4s.money.*

final case class QualifiedDividendsAndCapitalGainsWorksheet(
  ordinaryBrackets: OrdinaryRateFunction,
  qualifiedBrackets: QualifiedRateFunction
):
  // TODO: how to express this in an LP model?
  //  - compute tax on regular and investments separately
  //  - adjust brackets for non-investments by amount receiving preferential treatment?

  // Line 4
  def preferentiallyTaxedGains(form1040: Form1040): TaxableIncome =
    form1040.qualifiedDividends +
      form1040.scheduleD
        .map(_.netLongTermCapitalGains)
        .getOrElse(TaxableIncome.zero)
  end preferentiallyTaxedGains

  def taxOnPTGains: TaxPayable = ???

  def taxOnNonGains: TaxPayable = ???

  // Line 27
  def taxOnAllTaxableIncome: TaxPayable =
    // Lesser of:
    //   - preferential rates applied
    //   - no preferential rates applied
    ???
