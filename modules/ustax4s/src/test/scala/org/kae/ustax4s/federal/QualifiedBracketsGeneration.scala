package org.kae.ustax4s.federal

import org.kae.ustax4s.SetGeneration
import org.kae.ustax4s.money.IncomeThreshold
import org.scalacheck.Gen

trait QualifiedBracketsGeneration extends SetGeneration with TaxRateGeneration:

  // Somewhat realistic.
  private val genNonZeroIncomeThreshold: Gen[IncomeThreshold] =
    Gen.choose(10, 500000).map(IncomeThreshold.apply)

  // Notes:
  //   - lowest rate should always be zero% and start at zero.
  //   - at least one non-zero rate
  val genQualifiedBrackets: Gen[QualifiedIncomeBrackets] =
    for
      bracketCount <- Gen.choose(2, 10)
      nonZeroRates <- genSet(bracketCount - 1, genNonZeroTaxRate)
      ratesSortedAscending = FederalTaxRate.unsafeFrom(0.0) :: nonZeroRates.toList.sorted
      bracketBorders <- genSet(bracketCount - 1, genNonZeroIncomeThreshold)
      bracketStarts = bracketBorders + IncomeThreshold(0)
    yield QualifiedIncomeBrackets(
      bracketStarts.toList.sorted
        .zip(ratesSortedAscending)
        .toMap
    )
