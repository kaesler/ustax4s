package org.kae.ustax4s.money

opaque type TaxRate = Int

object TaxRate:

  def apply(i: Int): TaxRate = i

  extension (underlying: TaxRate) def asFraction: Double = underlying.toDouble / 100.0
end TaxRate
