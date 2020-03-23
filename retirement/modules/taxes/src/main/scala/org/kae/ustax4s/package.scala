package org.kae

import cats.kernel.Order
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.types.numeric.{NonNegBigDecimal, PosDouble, PosInt}
import scala.math.BigDecimal.RoundingMode

package object ustax4s {

  /**
    * Rate of tax payable in a given bracket.
    */
  type TaxRateRefinement = Interval.Closed[W.`0.0D`.T, W.`0.37D`.T]
  type TaxRate = Double Refined TaxRateRefinement
  object TaxRate {
    def unsafeFrom(d: Double): TaxRate = refineV[TaxRateRefinement](d).toOption.get
  }
  implicit val orderingForTaxRate: Ordering[TaxRate] = Ordering.by(_.value)
  implicit def orderedForTaxRate(tr: TaxRate): Ordered[TaxRate] =
    Ordered.orderingToOrdered(tr)

  /**
    * Cost-of-living based growth rates.
    */
  type InflationRate = Double Refined Interval.Closed[W.`0.0D`.T, W.`0.20D`.T]

  /**
    * Type for most tax calculations.
    */
  type TMoney = NonNegBigDecimal

  private val nnbd = NonNegBigDecimal

  implicit val orderingForTMoney: Ordering[TMoney] = Ordering.by(_.value)
  implicit val orderingForBigDecimal: Order[BigDecimal] = Order.fromOrdering[BigDecimal]
  implicit val orderForTMoney: Order[TMoney] =
    Order.by { tm: TMoney => tm.value }

  implicit class NonNegMoneyOps(val underlying: TMoney) {

    def rounded: TMoney = nnbd.unsafeFrom(
      underlying.value.setScale(0, RoundingMode.HALF_UP))

    def isZero: Boolean = underlying.value == NonNegMoneyOps.bdZero
    def nonZero: Boolean = ! isZero

    def +(other: TMoney): TMoney =
      nnbd.unsafeFrom(underlying.value + other.value)

    def -(other: TMoney): TMoney = subtract(other)
    /**
      * Subtract other [[TMoney]] but do not go below zero.
      *
      * @param other the [[NonNegBigDecimal]]
      * @return the result of the subtraction
      */
    def subtract(other: TMoney): TMoney =
      nnbd.unsafeFrom(
        (underlying.value - other.value).max(TMoney.zero.value))

    def min(other: TMoney): TMoney =
      nnbd.unsafeFrom(underlying.value min other.value)

    def *(rate: TaxRate): TMoney = nnbd.unsafeFrom(underlying.value * rate.value)

    def mul(fraction: PosDouble): TMoney = nnbd.unsafeFrom(underlying.value * fraction.value)

    def /(divisor: PosInt): TMoney = nnbd.unsafeFrom(underlying.value / divisor.value)
  }

  private object NonNegMoneyOps {
    private val bdZero = BigDecimal(0)
  }

  object TMoney {
    val zero: TMoney = NonNegBigDecimal.unsafeFrom(BigDecimal(0))

    def sum(ms: TMoney*): TMoney = nnbd.unsafeFrom(ms.map(_.value).sum)

    def u(i: Int): TMoney = NonNegBigDecimal.unsafeFrom(BigDecimal(i))

    def max(left: TMoney, right: TMoney): TMoney = orderForTMoney.max(left, right)
    def min(left: TMoney, right: TMoney): TMoney = orderForTMoney.min(left, right)
  }
}
