package org.kae.ustax4s

import org.scalacheck.Gen

trait TaxBracketsGeneration
  extends SetGeneration
  with TaxRateGeneration {

  // Somewhat realistic.
  private val genBracketBorder: Gen[TMoney] =
    Gen.choose(10, 500000).map(TMoney.u)

  val genTaxBrackets: Gen[OrdinaryIncomeTaxBrackets] = {
    for {
      bracketCount <- Gen.choose(1, 10)
      rates <- genSet(bracketCount, genTaxRate)
      // rates should always be progressive
      ratesSortedAscending = rates.toList.sorted
      bracketBorders <- genSet(bracketCount - 1, genBracketBorder)
    } yield OrdinaryIncomeTaxBrackets(
      (TMoney.zero :: bracketBorders.toList).sorted
        .zip(ratesSortedAscending)
        .toMap
    )
  }
}
