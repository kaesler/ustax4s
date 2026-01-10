package org.kae.ustax4s.money

import org.kae.ustax4s.SourceLoc
import org.kae.ustax4s.money.cmm.{CMM, CMMOps}

// Note: Opaque outside this file.
private[money] opaque type NonNegativeMoney = Money

private[money] object NonNegativeMoney:
  val zero: NonNegativeMoney = Money.zero

  // TODO: use refined types to check statically.
  // https://github.com/Iltotore/iron
  def apply(i: Int): NonNegativeMoney =
    require(i >= 0, s"attempt to create negative NonNegativeMoney from $i, at " + SourceLoc())
    Money(i)
  end apply

  def apply(d: Double): NonNegativeMoney =
    require(d >= 0, s"attempt to create negative NonNegativeMoney from $d, at " + SourceLoc())
    Money(d)
  end apply

  def unsafeParse(s: String): NonNegativeMoney =
    val i = Integer.parseInt(s)
    require(i >= 0, SourceLoc())
    Money(i)
  end unsafeParse

  def isZero(m: NonNegativeMoney): Boolean           = Money.isZero(m)
  def toDouble(n: NonNegativeMoney): Double          = Money.toDouble(n)
  def rounded(m: NonNegativeMoney): NonNegativeMoney = Money.rounded(m)

  def multiply(m: NonNegativeMoney, d: Double): NonNegativeMoney =
    require(d >= 0, s"multiplication by negative: $d, at " + SourceLoc())
    Money.multiply(m, d)
  end multiply

  def divide(m: NonNegativeMoney, i: Int): NonNegativeMoney =
    require(i > 0, s"division by non-positive: $i, at " + SourceLoc())
    Money.divide(m, i)
  end divide

  def divide(left: NonNegativeMoney, right: NonNegativeMoney): Double =
    Money.toDouble(left) / Money.toDouble(right)
  end divide

  def absoluteDifference(
    left: NonNegativeMoney,
    right: NonNegativeMoney
  ): NonNegativeMoney = Money.absoluteDifference(left, right)

  def areClose(left: NonNegativeMoney, right: NonNegativeMoney, tolerance: Int): Boolean =
    Money.toDouble(Money.absoluteDifference(left, right)) <= tolerance

  def monus(left: NonNegativeMoney, right: NonNegativeMoney): NonNegativeMoney =
    left monus right

  given monusOps: CMMOps           = new CMMOps {}
  given CMM[NonNegativeMoney]      = summon[CMM[Money]]
  given Ordering[NonNegativeMoney] = summon[Ordering[Money]]

end NonNegativeMoney
