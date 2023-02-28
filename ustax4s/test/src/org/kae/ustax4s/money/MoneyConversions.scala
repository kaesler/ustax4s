package org.kae.ustax4s.money

// Simplify test syntax.
object MoneyConversions:
  given Conversion[Int, Income]        = Income.apply
  given Conversion[Int, TaxableIncome] = TaxableIncome.apply
  given Conversion[Int, Deduction]     = Deduction.apply
end MoneyConversions
