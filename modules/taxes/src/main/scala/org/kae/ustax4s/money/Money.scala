package org.kae.ustax4s.money

import cats.Show
import cats.implicits.*
import org.kae.ustax4s.TaxRate
import org.kae.ustax4s.money.monus.Monus
import scala.math.BigDecimal.RoundingMode

private[money] type Money = BigDecimal
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

  def absoluteDifference(left: Money, right: Money): Money = (left - right).abs
  def add(left: Money, right: Money): Money                = left.combine(right)

  def areClose(left: Money, right: Money, tolerance: Int): Boolean =
    (left - right).abs <= tolerance

  def divide(m: Money, i: Int): Money =
    require(i > 0, s"division by non-positive: $i")
    m.toDouble / i.toDouble

  def divide(left: Money, right: Money): Double =
    require(right > 0, s"division by non-positive: $right")
    left.toDouble / right.toDouble

  def isZero(m: Money): Boolean  = m == 0
  def nonZero(m: Money): Boolean = m != 0

  def multiply(m: Money, d: Double): Money =
    require(d >= 0, s"multiplication by negative: $d")
    m * d

  def rounded(m: Money): Money = m.setScale(0, RoundingMode.HALF_UP)

  def subtractTruncated(left: Money, right: Money): Money =
    summon[Monus[Money]].subtractTruncated(left, right)

  def taxAt[T: TaxRate](m: Money, rate: T): Money = multiply(m, rate.asFraction)

  given Monus[Money]    = summonMonus
  given Ordering[Money] = summonOrdering
  given Show[Money]     = Show.fromToString[Money]

end Money

// Avoid infinite recursion by placing outside the Money object.
private def summonMonus    = summon[Monus[BigDecimal]]
private def summonOrdering = summon[Ordering[BigDecimal]]
