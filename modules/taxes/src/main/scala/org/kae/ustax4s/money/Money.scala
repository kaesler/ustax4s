package org.kae.ustax4s.money

import cats.kernel.Order
import scala.language.implicitConversions
import scala.math.BigDecimal.RoundingMode

/** Non negative money type.
  */
opaque type Money = BigDecimal

object Money:
  val zero: Money = 0

  def apply(i: Int): Money =
    require(i >= 0, s"attempt to create negative Money from $i")
    BigDecimal(i)

  def apply(d: Double): Money =
    require(d >= 0, s"attempt to create negative Money from $d")
    BigDecimal(d)

  def sum(ms: Money*): Money = ms.sum

  def max(left: Money, right: Money): Money =
    summon[Order[Money]].max(left, right)

  def min(left: Money, right: Money): Money =
    summon[Order[Money]].min(left, right)

  given Conversion[Int, Money]    = apply
  given Conversion[Double, Money] = apply

  given Ordering[Money] = summon[Ordering[BigDecimal]]
  given Order[Money]    = Order.fromOrdering[Money]

  extension (underlying: Money)
    def isZero: Boolean          = underlying == zero
    def nonZero: Boolean         = !isZero
    def rounded: Money           = underlying.setScale(0, RoundingMode.HALF_UP)
    def add(right: Money): Money = underlying + right
    def +(right: Money): Money   = add(right)

    infix def sub(right: Money): Money = zero.max(underlying - right)
    def -(right: Money): Money         = sub(right)

    infix def mul(i: Int): Money =
      require(i >= 0, s"multiplication by negative: $i")
      underlying * i

    infix def mul(d: Double): Money =
      require(d >= 0, s"multiplication by negative: $d")
      underlying * d

    infix def div(i: Int): Money =
      require(i > 0, s"division by non-positive: $i")
      underlying / i

    infix def div(m: Money): Double =
      require(m > 0, s"division by non-positive: $m")
      underlying.toDouble / m.toDouble
end Money
