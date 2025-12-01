package org.kae.ustax4s.federal

import org.kae.ustax4s.SetGeneration
import org.scalacheck.Gen

trait QualifiedBracketsGeneration extends SetGeneration with TaxRateGeneration:

  // Somewhat realistic.
  private val genNonZeroIncomeThresholdInt: Gen[Int] =
    Gen.choose(10, 500000)

  // Notes:
  //   - lowest rate should always be zero% and start at zero.
  //   - at least one non-zero rate
  val genQualifiedRateFunction: Gen[QualifiedRateFunction] =
    for
      bracketCount <- Gen.choose(2, 10)
      nonZeroRates <- genSet(bracketCount - 1, genNonZeroTaxRatePercentage)
      ratesSortedAscending = 0 :: nonZeroRates.toList.sorted
      bracketBorders <- genSet(bracketCount - 1, genNonZeroIncomeThresholdInt)
      bracketStarts = bracketBorders + 0
    yield QualifiedRateFunction.of(
      bracketStarts.toList.sorted
        .zip(ratesSortedAscending)
        .toMap
    )
