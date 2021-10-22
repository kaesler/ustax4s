package org.kae.ustax4s.federal.regimes

import org.kae.ustax4s.money.Money

trait FederalTaxCalculator:

  def federalTaxResults(
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    itemizedDeductions: Money
  ): FederalTaxResults

end FederalTaxCalculator
