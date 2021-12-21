package org.kae.ustax4s.taxfunction

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
// TODO: abstract to TaxRate
type TaxFunction = Income => TaxPayable

object TaxFunction:
  // TODO: need
  type Brackets[R] = Map[IncomeThreshold, R]

  // TODO: explain why the following works.
  def fromBrackets[R: TaxRate](brackets: Brackets[R]): TaxFunction =
    asRateDeltas(brackets)
      .map(makeThresholdTax[R].tupled)
      // Note: Tax has a natural Monoid because TaxPayable has one.
      .combineAll

  def makeFlatTax[R: TaxRate](rate: R): TaxFunction =
    makeThresholdTax(IncomeThreshold.zero, rate)

  def makeThresholdTax[R: TaxRate](
    threshold: IncomeThreshold,
    rate: R
  ): TaxFunction =
    _.amountAbove(threshold).taxAt(rate)

  private def asRateDeltas[R: TaxRate](brackets: Brackets[R]): List[(IncomeThreshold, R)] =
    brackets
      .keys
      .toList
      .sorted
      .zip(rateDeltas(brackets))

  private def rateDeltas[R: TaxRate](brackets: Brackets[R]): List[R] =
    val ratesWithZeroAtFront =
      summon[TaxRate[R]].zero :: brackets.values.toList.sorted
    ratesWithZeroAtFront
      .zip(ratesWithZeroAtFront.tail)
      .map { (previousRate, currentRate) =>
        currentRate delta previousRate
      }

end TaxFunction
