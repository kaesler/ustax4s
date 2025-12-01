package org.kae.ustax4s

import cats.implicits.*
import org.kae.ustax4s.money.{IncomeThreshold, TaxPayable, TaxableIncome}
import org.kae.ustax4s.{RateFunction, TaxRate}

// Note: function type here gives us a natural Monoid[TaxFunction].
type TaxFunction = TaxableIncome => TaxPayable

object TaxFunction:

  /** Return a function to apply a rate to all income over a specified threshold.
    */
  private def makeThresholdTax[R: TaxRate](
    threshold: IncomeThreshold,
    rate: R
  ): TaxFunction =
    _.amountAbove(threshold).taxAt(rate)

  /** Return a function to apply a rate to all the income.
    */
  def makeFlatTax[R: TaxRate](rate: R): TaxFunction =
    makeThresholdTax(IncomeThreshold.zero, rate)

  /** Return a function to apply a set of progressive tax brackets.
    */
  def fromRateFunction[R: TaxRate](brackets: RateFunction[R]): TaxFunction =
    // How this works:
    // Because taxes are progressive, with higher rates applying
    // in higher brackets, for each bracket threshold we pre-compute the
    // increase in tax rate from the previous threshold. Then we can just apply
    // that delta for a threshold for all income above the threshold, and sum the
    // results for all thresholds.
    // E.g. if we had a 10% bracket and a 20% bracket total tax is
    //   - 10% of all income above the 10% threshold, PLUS
    //   - (20% - 10%) of all income above the 20% threshold.

    asRateDeltas(brackets)
      .map(makeThresholdTax[R].tupled)
      // Note: TaxFunction has a natural Monoid because TaxPayable has one.
      .combineAll
  end fromRateFunction

  private def asRateDeltas[R: TaxRate](brackets: RateFunction[R]): Vector[(IncomeThreshold, R)] =
    brackets.thresholdsAscending
      .zip(rateDeltas(brackets))
  end asRateDeltas

  private def rateDeltas[R: TaxRate](brackets: RateFunction[R]): List[R] =
    val ratesWithZeroAtFront =
      summon[TaxRate[R]].zero :: brackets.rates.toList.sorted
    ratesWithZeroAtFront
      .zip(ratesWithZeroAtFront.tail)
      .map: (previousRate, currentRate) =>
        currentRate delta previousRate
  end rateDeltas

end TaxFunction
