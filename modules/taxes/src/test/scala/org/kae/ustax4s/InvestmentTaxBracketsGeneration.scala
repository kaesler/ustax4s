package org.kae.ustax4s

import org.scalacheck.Gen

trait InvestmentTaxBracketsGeneration extends SetGeneration with TaxRateGeneration {

  // Somewhat realistic.
  private val genNonZeroBracketBorder: Gen[TMoney] =
    Gen.choose(10, 500000).map(TMoney.u)

  // Notes:
  //   - lowest rate should always be zero% and start at zero.
  //   - at least one non-zero rate
  val genInvestmentTaxBrackets: Gen[InvestmentIncomeTaxBrackets] = {
    for {
      bracketCount <- Gen.choose(2, 10)
      nonZeroRates <- genSet(bracketCount - 1, genNonZeroTaxRate)
      ratesSortedAscending = TaxRate.unsafeFrom(0.0) :: nonZeroRates.toList.sorted
      bracketBorders <- genSet(bracketCount - 1, genNonZeroBracketBorder)
      bracketStarts = bracketBorders + TMoney.zero
    } yield InvestmentIncomeTaxBrackets(
      bracketStarts.toList.sorted
        .zip(ratesSortedAscending)
        .toMap
    )
  }
}
