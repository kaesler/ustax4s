package org.kae.ustax4s.money

import cats.implicits.*
import org.kae.ustax4s.money.cmm.{CMM, CMMOps}

private[money] type Money = Double
extension (m: Money) def rounded: Money = math.round(m).toDouble

object Money:

  given monusOps: CMMOps = new CMMOps {}

  val zero: Money = 0

  def apply(i: Int): Money =
    require(i >= 0, s"attempt to create negative Money from $i")
    i.toDouble

  def apply(d: Double): Money =
    require(d >= 0, s"attempt to create negative Money from $d")
    d

  def unsafeParse(s: String): Money =
    val d = java.lang.Double.valueOf(s)
    require(d >= 0.0)
    d

  def absoluteDifference(left: Money, right: Money): Money = (left - right).abs

  def areClose(left: Money, right: Money, tolerance: Int): Boolean =
    (left - right).abs <= tolerance

  def divide(m: Money, i: Int): Money =
    require(i > 0, s"division by non-positive: $i")
    m / i.toDouble

  def divide(left: Money, right: Money): Double =
    require(right > 0, s"division by non-positive: $right")
    left / right

  def isZero(m: Money): Boolean = m == 0

  def multiply(m: Money, d: Double): Money =
    require(d >= 0, s"multiplication by negative: $d")
    m * d

  def round(m: Money): Money = math.round(m).toDouble

  def monus(left: Money, right: Money): Money = left monus right

  given CMM[Money]      = summonMonus
  given Ordering[Money] = summonOrdering

end Money

// Avoid infinite recursion by placing outside the Money object.
private def summonMonus    = summon[CMM[Double]]
private def summonOrdering = summon[Ordering[Double]]
