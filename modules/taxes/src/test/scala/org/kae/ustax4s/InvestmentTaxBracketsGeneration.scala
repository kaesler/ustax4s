package org.kae.ustax4s

import org.scalacheck.Gen

trait InvestmentTaxBracketsGeneration
  extends SetGeneration
  with TaxRateGeneration {

  // Somewhat realistic.
  private val genBracketBorder: Gen[TMoney] =
    Gen.choose(10, 500000).map(TMoney.u)

  val genCgTaxBrackets: Gen[InvestmentIncomeTaxBrackets] = {
    for {
      bracketCount <- Gen.choose(1, 10)
      rates <- genSet(bracketCount, genTaxRate)
      ratesSortedAscending = rates.toList.sorted
      bracketBorders <- genSet(bracketCount - 1, genBracketBorder)
    } yield InvestmentIncomeTaxBrackets(
      (TMoney.zero :: bracketBorders.toList).sorted
        .zip(ratesSortedAscending)
        .toMap
    )
  }
}
