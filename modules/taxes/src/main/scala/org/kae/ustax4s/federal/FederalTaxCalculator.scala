package org.kae.ustax4s.federal

import java.time.{LocalDate, Year}
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.{FilingStatus, InflationEstimate}

trait FederalTaxCalculator:

  // Note: We could have just used cyrrying, i.e.
  //   trait FederalTaxCalculator extends ((Money .. Money) => FederalTaxResults
  // but it is more ergonomic to allow named arguments.

  // Compute tax results for the given inputs.
  // By default, un-inflated tax brackets, deductions and so ond are used.
  def federalTaxResults(
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    itemizedDeductions: Money
  ): FederalTaxResults

end FederalTaxCalculator
