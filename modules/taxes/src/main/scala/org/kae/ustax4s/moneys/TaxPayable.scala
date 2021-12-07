package org.kae.ustax4s.moneys

import cats.Monoid
import cats.implicits.*
import org.kae.ustax4s.money.Money

opaque type TaxPayable = Money

// TODO: The result of applying a tax rate to an Income?
object TaxPayable:

  def apply(i: Int): TaxPayable    = Money(i)
  def apply(d: Double): TaxPayable = Money(d)

  given Monoid[TaxPayable] = summonMonoid

  extension (underlying: TaxPayable)
    def +(right: TaxPayable): TaxPayable = underlying.combine(right)
  end extension

end TaxPayable

// Avoid infinite recursion by placing outside the Money object.
private def summonMonoid = summon[Monoid[Money]]
