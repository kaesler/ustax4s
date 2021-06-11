package org.kae.ustax4s.money

opaque type IntTaxRate = Int

object IntTaxRate:

  def apply(percentage: Int): IntTaxRate =
    require(percentage >= 0)
    require(percentage < 50)
    percentage

  extension (underlying: IntTaxRate) def asFraction: Double = underlying.toDouble / 100.0
end IntTaxRate
