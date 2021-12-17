package org.kae.ustax4s.federal

import org.kae.ustax4s.SetGeneration
import org.kae.ustax4s.money.IncomeThreshold
import org.scalacheck.Gen

trait TaxBracketsGeneration extends SetGeneration with TaxRateGeneration:

  // Somewhat realistic.
  private val genIncomeThreshold: Gen[IncomeThreshold] =
    Gen.choose(10, 500000).map(IncomeThreshold.apply)

  val genTaxBrackets: Gen[OrdinaryIncomeBrackets] =
    for
      bracketCount <- Gen.choose(1, 10)
      rates        <- genSet(bracketCount, genNonZeroTaxRate)
      // rates should always be progressive
      ratesSortedAscending = rates.toList.sorted
      bracketBorders <- genSet(bracketCount - 1, genIncomeThreshold)
    yield OrdinaryIncomeBrackets(
      (IncomeThreshold.zero :: bracketBorders.toList).sorted
        .zip(ratesSortedAscending)
        .toMap
    )
