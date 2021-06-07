package org.kae.ustax4s.money

opaque type TaxRate = Int

object TaxRate:

  def apply(percentage: Int): TaxRate =
    require(percentage >= 0)
    require(percentage < 50)
    percentage

  extension (underlying: TaxRate) def asFraction: Double = underlying.toDouble / 100.0
end TaxRate
