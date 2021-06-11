package org.kae.ustax4s.money

object MoneySyntax:

  extension (i: Int) def asMoney: TMoney    = TMoney(i)
  extension (d: Double) def asMoney: TMoney = TMoney(d)

end MoneySyntax
