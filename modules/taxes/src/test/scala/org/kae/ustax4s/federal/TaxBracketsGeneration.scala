package org.kae.ustax4s.federal

import org.kae.ustax4s.SetGeneration
import org.kae.ustax4s.money.Money
import org.scalacheck.Gen

trait TaxBracketsGeneration extends SetGeneration with TaxRateGeneration:

  // Somewhat realistic.
  private val genBracketBorder: Gen[Money] =
    Gen.choose(10, 500000).map(Money.apply)

  val genTaxBrackets: Gen[OrdinaryIncomeBrackets] =
    for
      bracketCount <- Gen.choose(1, 10)
      rates        <- genSet(bracketCount, genNonZeroTaxRate)
      // rates should always be progressive
      ratesSortedAscending = rates.toList.sorted
      bracketBorders <- genSet(bracketCount - 1, genBracketBorder)
    yield OrdinaryIncomeBrackets(
      (Money(0) :: bracketBorders.toList).sorted
        .zip(ratesSortedAscending)
        .toMap
    )
