package org.kae.ustax4s.money

import org.kae.ustax4s.money.cmm.CMM
import scala.math.BigDecimal.RoundingMode

// Note: Opaque outside this file.
private[money] opaque type Money = BigDecimal

private[money] object Money:
  val zero: Money = 0

  def apply(i: Int): Money          = BigDecimal(i)
  def apply(d: Double): Money       = BigDecimal(d)
  def unsafeParse(s: String): Money = BigDecimal(Integer.parseInt(s))

  def isZero(m: Money): Boolean         = m == 0
  def rounded(m: Money): Money          = m.setScale(0, RoundingMode.HALF_UP)
  inline def toDouble(m: Money): Double = m.toDouble

  def absoluteDifference(left: Money, right: Money): Money = (left - right).abs
  def divide(left: Money, right: Int): Money               = left / right
  def multiply(m: Money, d: Double): Money                 = m * d

  given CMM[Money]      = summonMonus
  given Ordering[Money] = summonOrdering
end Money

// Avoid infinite recursion by placing outside the Money object.
private def summonMonus    = summon[CMM[BigDecimal]]
private def summonOrdering = summon[Ordering[BigDecimal]]
