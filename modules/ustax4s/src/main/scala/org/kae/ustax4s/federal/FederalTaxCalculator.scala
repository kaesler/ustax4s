package org.kae.ustax4s.federal

import java.time.LocalDate
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome}

trait FederalTaxCalculator:

  // Note: We could have just used cyrrying, i.e.
  //   trait FederalTaxCalculator extends ((Money .. Money) => FederalTaxResults
  // but it is more ergonomic to allow named arguments.

  // Compute tax results for the given inputs.
  // By default, un-inflated tax brackets, deductions and so on are used.
  def federalTaxResults(
    birthDate: LocalDate,
    personalExemptions: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  ): FederalTaxResults

end FederalTaxCalculator
