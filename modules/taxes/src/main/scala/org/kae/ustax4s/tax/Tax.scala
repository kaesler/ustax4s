package org.kae.ustax4s.tax

import cats.Monoid
import cats.implicits.*
import org.kae.ustax4s.TaxRate
import org.kae.ustax4s.federal.{FederalTaxRate, OrdinaryIncomeBrackets, QualifiedIncomeBrackets}
import org.kae.ustax4s.money.{Income, IncomeThreshold, TaxPayable, TaxableIncome}

// TODO: terminology: tax is a verb and a noun. TaxFunc?
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
    asRateDeltas(brackets)
      .map(makeThresholdTax.tupled)
      // Note: Tax has a natural Monoid because TaxPayable has one.
      .combineAll

  def makeFlatTax(rate: FederalTaxRate): Tax =
    makeThresholdTax(IncomeThreshold.zero, rate)

  def makeThresholdTax(
    threshold: IncomeThreshold,
    rate: FederalTaxRate
  ): Tax =
    _.amountAbove(threshold).taxAt(rate)

  private def asRateDeltas(
    brackets: OrdinaryIncomeBrackets
  ): List[(IncomeThreshold, FederalTaxRate)] =
    brackets.bracketStarts
      .keys
      .toList
      .sorted
      .zip(rateDeltas(brackets))

  private def rateDeltas(brackets: OrdinaryIncomeBrackets): List[FederalTaxRate] =
    val ratesWithZeroAtFront =
      FederalTaxRate.zero :: brackets.bracketStarts.values.toList.sorted
    ratesWithZeroAtFront
      .zip(ratesWithZeroAtFront.tail)
      .map { (previousRate, currentRate) =>
        currentRate absoluteDifference previousRate
      }

end Tax
