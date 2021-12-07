package org.kae.ustax4s.moneys

import cats.Monoid
import cats.Show
import cats.implicits.*
import org.kae.ustax4s.money.Money

opaque type Deduction = Money

object Deduction:

  def apply(i: Int): Deduction    = Money(i)
  def apply(d: Double): Deduction = Money(d)

  given Monoid[Deduction] = summonMonoid
  given Show[Deduction]   = summonShow

  extension (underlying: Deduction)
    def +(right: Deduction): Deduction    = underlying.combine(right)
    def subtractFrom(money: Money): Money = underlying subp money
  end extension

end Deduction
private def summonMonoid = summon[Monoid[Money]]
private def summonShow   = summon[Show[Money]]
