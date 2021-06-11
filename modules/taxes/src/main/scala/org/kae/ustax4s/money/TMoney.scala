package org.kae.ustax4s.money

import cats.kernel.Order
import scala.math.BigDecimal.RoundingMode
import org.kae.ustax4s.TaxRate

/** Non negative money type.
  */
opaque type TMoney = BigDecimal

object TMoney:
  val zero: TMoney = 0

  def apply(i: Int): TMoney =
    require(i >= 0, s"attempt to create negative Money from $i")
    BigDecimal(i)

  def apply(d: Double): TMoney =
    require(d >= 0, s"attempt to create negative Money from $d")
    BigDecimal(d)

  def unsafeParse(s: String): TMoney =
    val i = Integer.parseInt(s)
    require(i >= 0)
    BigDecimal(i)

  def sum(ms: TMoney*): TMoney = ms.sum

  def max(left: TMoney, right: TMoney): TMoney =
    summon[Order[TMoney]].max(left, right)

  def min(left: TMoney, right: TMoney): TMoney =
    summon[Order[TMoney]].min(left, right)

  given Conversion[Int, TMoney]    = apply
  given Conversion[Double, TMoney] = apply

  // Note: careful to avoid recursive instance here.
  // This seems to do it.
  given Ordering[TMoney] with
    def compare(x: TMoney, y: TMoney) =
      if x < y then -1 else if x > y then +1 else 0

  extension (underlying: TMoney)
    def isZero: Boolean            = underlying == zero
    def nonZero: Boolean           = !isZero
    def rounded: TMoney            = underlying.setScale(0, RoundingMode.HALF_UP)
    def add(right: TMoney): TMoney = underlying + right
    def +(right: TMoney): TMoney   = add(right)

    infix def sub(right: TMoney): TMoney = zero.max(underlying - right)
    def -(right: TMoney): TMoney         = sub(right)

    infix def mul(i: Int): TMoney =
      require(i >= 0, s"multiplication by negative: $i")
      underlying * i

    infix def mul(d: Double): TMoney =
      require(d >= 0, s"multiplication by negative: $d")
      underlying * d

    infix def div(i: Int): TMoney =
      require(i > 0, s"division by non-positive: $i")
      underlying / i

    infix def div(m: TMoney): TMoney =
      require(m > 0, s"division by non-positive: $m")
      underlying.toDouble / m.toDouble

    // Compute tax at the given rate.
    infix def taxAt[T: TaxRate](rate: T): TMoney =
      underlying mul rate.asFraction

    infix def <(that: TMoney): Boolean =
      underlying.compare(that) < 0
    infix def >(that: TMoney): Boolean =
      underlying.compare(that) > 0
    infix def <=(that: TMoney): Boolean = !(underlying > that)
    infix def >=(that: TMoney): Boolean = !(underlying < that)
//underlying.compare(that) < 0

end TMoney
