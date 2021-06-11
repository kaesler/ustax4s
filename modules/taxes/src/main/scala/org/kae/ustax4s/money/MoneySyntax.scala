package org.kae.ustax4s.money

object MoneySyntax:

  extension (i: Int) def asMoney: Money    = Money(i)
  extension (d: Double) def asMoney: Money = Money(d)

end MoneySyntax
