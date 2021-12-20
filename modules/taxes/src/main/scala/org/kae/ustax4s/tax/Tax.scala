package org.kae.ustax4s.tax

import cats.Monoid
import cats.implicits.*
import org.kae.ustax4s.TaxRate
import org.kae.ustax4s.federal.{FederalTaxRate, OrdinaryIncomeBrackets, QualifiedIncomeBrackets}
import org.kae.ustax4s.money.{Income, IncomeThreshold, TaxPayable, TaxableIncome}

// Note: function type here gives us a natural Monoid[Tax].
//  TODO: Is the natural Monus[Tax] useful?
//  fed tax = ordBracketTax(ordinaryIncome) + qualBracketTax(ord + qual) - qualBracketTax(ord)
// i.e. perhaps (ordBracketTax - qualBracketTax)(ordIncome) + qualBracketTax(ord + qual)
// Is this right?
// TODO: Tighten up to be TaxableIncome => TaxPayable
type Tax = Income => TaxPayable

object Tax:
  // TODO: explain why the following works.
  def fromBrackets(brackets: OrdinaryIncomeBrackets): Tax =
    brackets
      .asRateDeltas
      .map((threshold, rate) => makeThresholdTax(rate, threshold))
      // Note: Tax has a natural Monoid because TaxPayable has one.
      .combineAll

  def makeFlatTax(rate: FederalTaxRate): Tax =
    makeThresholdTax(rate, IncomeThreshold.zero)

  def makeThresholdTax(
    rate: FederalTaxRate,
    threshold: IncomeThreshold
  ): Tax =
    taxableIncome => taxableIncome.amountAbove(threshold).taxAt(rate)

end Tax
