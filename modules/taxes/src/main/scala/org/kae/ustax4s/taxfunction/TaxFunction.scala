package org.kae.ustax4s.taxfunction

import cats.Monoid
import cats.implicits.*
import org.kae.ustax4s.TaxRate
import org.kae.ustax4s.federal.{FederalTaxRate, OrdinaryIncomeBrackets, QualifiedIncomeBrackets}
import org.kae.ustax4s.money.{Income, IncomeThreshold, TaxPayable, TaxableIncome}

// Note: function type here gives us a natural Monoid[Tax].
type TaxFunction = TaxableIncome => TaxPayable

object TaxFunction:
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
