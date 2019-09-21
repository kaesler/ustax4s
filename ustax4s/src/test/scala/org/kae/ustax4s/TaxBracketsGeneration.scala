package org.kae.ustax4s

import org.scalacheck.Gen

trait TaxBracketsGeneration
  extends SetGeneration
  with TaxRateGeneration {

  // Somewhat realistic.
  private val genBracketBorder: Gen[TMoney] =
    Gen.choose(10, 500000).map(TMoney.u)

  val genTaxBrackets: Gen[TaxBrackets] = {
    for {
      bracketCount <- Gen.choose(1, 10)
      rates <- genSet(bracketCount, genTaxRate)
      bracketBorders <- genSet(bracketCount - 1, genBracketBorder)
    } yield TaxBrackets(
      (TMoney.zero :: bracketBorders.toList).sorted
        .zip(rates)
        .toMap
    )
  }
}
