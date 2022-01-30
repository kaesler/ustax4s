package org.kae.ustax4s.federal

import org.kae.ustax4s.SetGeneration
import org.scalacheck.Gen

trait TaxBracketsGeneration extends SetGeneration with TaxRateGeneration:

  // Somewhat realistic.
  private val genIncomeThresholdInt: Gen[Int] =
    Gen.choose(10, 500000)

  val genTaxBrackets: Gen[OrdinaryBrackets] =
    for
      bracketCount <- Gen.choose(1, 10)
      rates        <- genSet(bracketCount, genNonZeroTaxRateDouble)
      // rates should always be progressive
      ratesSortedAscending = rates.toList.sorted
      bracketBorders <- genSet(bracketCount - 1, genIncomeThresholdInt)
    yield OrdinaryBrackets.create(
      (0 :: bracketBorders.toList).sorted
        .zip(ratesSortedAscending)
        .toMap
    )
