package org.kae.ustax4s

import org.kae.ustax4s.money.Money

// Simplify test syntax.
object MoneyConversions:
  given Conversion[Int, Money] = Money.apply
end MoneyConversions
