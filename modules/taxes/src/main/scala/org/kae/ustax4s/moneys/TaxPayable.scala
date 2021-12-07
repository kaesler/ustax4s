package org.kae.ustax4s.moneys

import cats.Monoid
import cats.Show
import cats.implicits.*
import org.kae.ustax4s.money.Money

opaque type TaxPayable = Money

// TODO: The result of applying a tax rate to an Income?
object TaxPayable:

  def apply(i: Int): TaxPayable    = Money(i)
  def apply(d: Double): TaxPayable = Money(d)

  given Monoid[TaxPayable] = summonMonoid
  given Show[TaxPayable]   = summonShow

  extension (underlying: TaxPayable)
    def +(right: TaxPayable): TaxPayable = underlying.combine(right)
  end extension

end TaxPayable
private def summonMonoid = summon[Monoid[Money]]
private def summonShow   = summon[Show[Money]]
