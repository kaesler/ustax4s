package org.kae.ustax4s.money

import org.kae.ustax4s.SourceLoc
import org.kae.ustax4s.money.cmm.{CMM, CMMOps}
import scala.math.BigDecimal.RoundingMode

private[money] opaque type NonNegativeMoney = Money

private object NonNegativeMoney:
  val zero: NonNegativeMoney = Money.zero

  def isZero(m: NonNegativeMoney): Boolean  = Money.isZero(m)
  def toDouble(n: NonNegativeMoney): Double = Money.toDouble(n)

  // TODO: use refined types to check statically?
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

  def rounded(m: NonNegativeMoney): NonNegativeMoney = m.setScale(0, RoundingMode.HALF_UP)

  def multiply(m: NonNegativeMoney, d: Double): NonNegativeMoney =
    require(d >= 0, s"multiplication by negative: $d, at " + SourceLoc())
    m * d
  end multiply

  def divide(m: NonNegativeMoney, i: Int): NonNegativeMoney =
    require(i > 0, s"division by non-positive: $i, at " + SourceLoc())
    m.toDouble / i.toDouble
  end divide

  def divide(left: NonNegativeMoney, right: NonNegativeMoney): Double =
    require(right > 0, s"division by non-positive: $right, at " + SourceLoc())
    left.toDouble / right.toDouble
  end divide

  def absoluteDifference(
    left: NonNegativeMoney,
    right: NonNegativeMoney
  ): NonNegativeMoney = Money.absoluteDifference(left, right)

  def areClose(left: NonNegativeMoney, right: NonNegativeMoney, tolerance: Int): Boolean =
    (left - right).abs <= tolerance

  def monus(left: NonNegativeMoney, right: NonNegativeMoney): NonNegativeMoney =
    left monus right

  given monusOps: CMMOps           = new CMMOps {}
  given CMM[NonNegativeMoney]      = summon[CMM[Money]]
  given Ordering[NonNegativeMoney] = summon[Ordering[Money]]

end NonNegativeMoney
