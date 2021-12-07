package org.kae.ustax4s.money

import cats.Monoid
import cats.implicits.*
import cats.kernel.Order
import org.kae.ustax4s.TaxRate
import scala.math.BigDecimal.RoundingMode

// TODO: eventually make this type package private.
// TODO: eventually remove many unneeded methods.

/** Non negative money type.
  */
opaque type Money = BigDecimal

object Money:

  given Monoid[Money]             = summonMonoid
  given Ordering[Money]           = summonOrdering
  given Conversion[Int, Money]    = apply
  given Conversion[Double, Money] = apply

  private val zero: Money = 0

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

  extension (underlying: Money)
    def isZero: Boolean        = underlying == 0
    def nonZero: Boolean       = !isZero
    def rounded: Money         = underlying.setScale(0, RoundingMode.HALF_UP)
    def +(right: Money): Money = underlying.combine(right)

    // Subtract but don't go negative.
    infix def subp(right: Money): Money = zero.max(underlying - right)

    infix def mul(d: Double): Money =
      require(d >= 0, s"multiplication by negative: $d")
      underlying * d

    infix def div(m: Money): Money =
      require(m > 0, s"division by non-positive: $m")
      underlying.toDouble / m.toDouble

    // Compute tax at the given rate.
    infix def taxAt[T: TaxRate](rate: T): Money = underlying mul rate.asFraction

    // Note: there seems no way to have Money "extends Ordered[Money]"
    // and then just provide the compare() method, so this is what we do.
    infix def <(that: Money): Boolean  = underlying.compare(that) < 0
    infix def >(that: Money): Boolean  = underlying.compare(that) > 0
    infix def <=(that: Money): Boolean = !(underlying > that)
    infix def >=(that: Money): Boolean = !(underlying < that)

    def isCloseTo(that: Money, tolerance: Int): Boolean =
      (underlying - that).abs <= tolerance

  end extension

end Money

// Avoid infinite recursion by placing outside the Money object.
private def summonMonoid   = summon[Monoid[BigDecimal]]
private def summonOrdering = summon[Ordering[BigDecimal]]
