package org.kae.ustax4s

import org.kae.ustax4s.money.{Deduction, Income}

// Simplify test syntax.
object MoneyConversions:
  given Conversion[Int, Income]    = Income.apply
  given Conversion[Int, Deduction] = Deduction.apply
end MoneyConversions
