package org.kae.ustax4s.money

import cats.implicits.*
import org.kae.ustax4s.SourceLoc
import org.kae.ustax4s.money.cmm.{CMM, CMMOps}
import scala.math.BigDecimal.RoundingMode

private[money] type Money = BigDecimal
object Money:

  given monusOps: CMMOps = new CMMOps {}

  val zero: Money = 0

  def apply(i: Int): Money =
    require(i >= 0, s"attempt to create negative Money from $i, at " + SourceLoc.loc)
    BigDecimal(i)

  def apply(d: Double): Money =
    require(d >= 0, s"attempt to create negative Money from $d, at " + SourceLoc.loc)
    BigDecimal(d)

  def unsafeParse(s: String): Money =
    val i = Integer.parseInt(s)
    require(i >= 0, SourceLoc.loc)
    BigDecimal(i)

  def absoluteDifference(left: Money, right: Money): Money = (left - right).abs

  def areClose(left: Money, right: Money, tolerance: Int): Boolean =
    (left - right).abs <= tolerance

  def divide(m: Money, i: Int): Money =
    require(i > 0, s"division by non-positive: $i, at " + SourceLoc.loc)
    m.toDouble / i.toDouble

  def divide(left: Money, right: Money): Double =
    require(right > 0, s"division by non-positive: $right, at " + SourceLoc.loc)
    left.toDouble / right.toDouble

  def isZero(m: Money): Boolean = m == 0

  def multiply(m: Money, d: Double): Money =
    require(d >= 0, s"multiplication by negative: $d, at " + SourceLoc.loc)
    m * d

  def rounded(m: Money): Money = m.setScale(0, RoundingMode.HALF_UP)

  def monus(left: Money, right: Money): Money = left monus right

  given CMM[Money]      = summonMonus
  given Ordering[Money] = summonOrdering

end Money

// Avoid infinite recursion by placing outside the Money object.
private def summonMonus    = summon[CMM[BigDecimal]]
private def summonOrdering = summon[Ordering[BigDecimal]]
