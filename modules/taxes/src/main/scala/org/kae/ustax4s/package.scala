package org.kae

import cats.kernel.Order
import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.types.numeric.{NonNegBigDecimal, PosDouble, PosInt}
import scala.language.implicitConversions
import scala.math.BigDecimal.RoundingMode

import org.kae.ustax4s.federal.FederalTaxRate
import org.kae.ustax4s.state.StateTaxRate

package object ustax4s:

  /** Type for most tax calculations.
    */
  type TMoney = NonNegBigDecimal

  private val nnbd = NonNegBigDecimal

  given Ordering[TMoney] = Ordering.by(_.value)

  given Order[TMoney] = Order.by(_.value)

  extension (i: Int) def asMoney: TMoney = TMoney(i)

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

    def taxAt(rate: FederalTaxRate): TMoney =
      nnbd.unsafeFrom(underlying.value * rate.value)

    def stateTaxAt(rate: StateTaxRate): TMoney =
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

    def unsafeParse(s: String): TMoney =
      NonNegBigDecimal.unsafeFrom(BigDecimal(Integer.parseInt(s)))

    def sum(ms: TMoney*): TMoney = nnbd.unsafeFrom(ms.map(_.value).sum)

    def apply(i: Int): TMoney = NonNegBigDecimal.unsafeFrom(BigDecimal(i))

    def max(left: TMoney, right: TMoney): TMoney =
      summon[Order[TMoney]].max(left, right)

    def min(left: TMoney, right: TMoney): TMoney =
      summon[Order[TMoney]].min(left, right)
