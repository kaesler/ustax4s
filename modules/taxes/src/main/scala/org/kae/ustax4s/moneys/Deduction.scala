package org.kae.ustax4s.moneys

import cats.Monoid
import cats.implicits.*
import org.kae.ustax4s.money.Money

opaque type Deduction = Money

object Deduction:

  def apply(i: Int): Deduction    = Money(i)
  def apply(d: Double): Deduction = Money(d)

  given Monoid[Deduction] = summonMonoid

  extension (underlying: Deduction)
    def +(right: Deduction): Deduction    = underlying.combine(right)
    def subtractFrom(money: Money): Money = underlying subp money
  end extension

end Deduction

// Avoid infinite recursion by placing outside the Money object.
private def summonMonoid = summon[Monoid[Money]]
