package org.kae

import cats.kernel.Order
import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.types.numeric.{NonNegBigDecimal, PosDouble, PosInt}
import scala.language.implicitConversions
import scala.math.BigDecimal.RoundingMode

package object ustax4s:

  // TODO use Scala3 opaque types and extension methods here.
  // TODO: use integers for tax rates as in Haskell/Purescript code.
  // Or represent as a finite discrete set

  /** Rate of tax payable in a given bracket.
    */
  type TaxRateRefinement = Interval.Closed[0.0d, 0.37d]
  type TaxRate           = Double Refined TaxRateRefinement

  object TaxRate {

    def unsafeFrom(d: Double): TaxRate =
      refineV[TaxRateRefinement](d).toOption.get
  }
  given Ordering[TaxRate] = Ordering.by(_.value)

  // TODO: Use Scala3?  Type class?
  implicit def orderedForTaxRate(tr: TaxRate): Ordered[TaxRate] =
    Ordered.orderingToOrdered(tr)

  /** Cost-of-living based growth rates.
    */
  type InflationRate = Double Refined Interval.Closed[0.0d, 0.20d]

  /** Type for most tax calculations.
    */
  type TMoney = NonNegBigDecimal

  private val nnbd = NonNegBigDecimal

  given Ordering[TMoney] = Ordering.by(_.value)

  given Order[TMoney] = Order.by(_.value)

  // TODO: Use Scala3
  implicit class NonNegMoneyOps(val underlying: TMoney):

    def rounded: TMoney =
      nnbd.unsafeFrom(underlying.value.setScale(0, RoundingMode.HALF_UP))

    def isZero: Boolean  = underlying.value == NonNegMoneyOps.bdZero
    def nonZero: Boolean = !isZero

    def +(other: TMoney): TMoney =
      nnbd.unsafeFrom(underlying.value + other.value)

    def -(other: TMoney): TMoney = subtract(other)

    /** Subtract other [[TMoney]] but do not go below zero.
      *
      * @param other
      *   the [[NonNegBigDecimal]]
      * @return
      *   the result of the subtraction
      */
    def subtract(other: TMoney): TMoney =
      nnbd.unsafeFrom((underlying.value - other.value).max(TMoney.zero.value))

    def *(rate: TaxRate): TMoney =
      nnbd.unsafeFrom(underlying.value * rate.value)

    def mul(fraction: PosDouble): TMoney =
      nnbd.unsafeFrom(underlying.value * fraction.value)

    def mul(i: Int): TMoney = nnbd.unsafeFrom(underlying.value * i)

    def /(divisor: PosInt): TMoney =
      nnbd.unsafeFrom(underlying.value / divisor.value)

    def div(divisor: TMoney): Double =
      underlying.value.toDouble / divisor.value.toDouble

  private object NonNegMoneyOps:
    private val bdZero = BigDecimal(0)

  object TMoney:
    val zero: TMoney = NonNegBigDecimal.unsafeFrom(BigDecimal(0))

    def sum(ms: TMoney*): TMoney = nnbd.unsafeFrom(ms.map(_.value).sum)

    def u(i: Int): TMoney = NonNegBigDecimal.unsafeFrom(BigDecimal(i))

    def max(left: TMoney, right: TMoney): TMoney =
      summon[Order[TMoney]].max(left, right)

    def min(left: TMoney, right: TMoney): TMoney =
      summon[Order[TMoney]].min(left, right)
