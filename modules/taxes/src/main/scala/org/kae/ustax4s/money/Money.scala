package org.kae.ustax4s.money

import cats.kernel.Order
import org.kae.ustax4s.TaxRate
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

  def unsafeParse(s: String): Money =
    val i = Integer.parseInt(s)
    require(i >= 0)
    BigDecimal(i)

  def sum(ms: Money*): Money = ms.sum

  def max(left: Money, right: Money): Money =
    summon[Order[Money]].max(left, right)

  def min(left: Money, right: Money): Money =
    summon[Order[Money]].min(left, right)

  given Conversion[Int, Money]    = apply
  given Conversion[Double, Money] = apply

  // Note: careful to avoid recursive instance here.
  // This seems to do it.
  given Ordering[Money] with
    def compare(x: Money, y: Money): Int =
      if x < y then -1 else if x > y then +1 else 0

  extension (underlying: Money)
    def isZero: Boolean          = underlying == zero
    def nonZero: Boolean         = !isZero
    def rounded: Money           = underlying.setScale(0, RoundingMode.HALF_UP)
    def add(right: Money): Money = underlying + right
    def +(right: Money): Money   = add(right)

    // Subtract but don't go negative.
    infix def subp(right: Money): Money = zero.max(underlying - right)

    infix def mul(i: Int): Money =
      require(i >= 0, s"multiplication by negative: $i")
      underlying * i

    infix def mul(d: Double): Money =
      require(d >= 0, s"multiplication by negative: $d")
      underlying * d

    infix def div(i: Int): Money =
      require(i > 0, s"division by non-positive: $i")
      underlying / i

    infix def div(m: Money): Money =
      require(m > 0, s"division by non-positive: $m")
      underlying.toDouble / m.toDouble

    // Compute tax at the given rate.
    infix def taxAt[T: TaxRate](rate: T): Money =
      underlying mul rate.asFraction

    infix def <(that: Money): Boolean =
      underlying.compare(that) < 0
    infix def >(that: Money): Boolean =
      underlying.compare(that) > 0
    infix def <=(that: Money): Boolean = !(underlying > that)
    infix def >=(that: Money): Boolean = !(underlying < that)

end Money
