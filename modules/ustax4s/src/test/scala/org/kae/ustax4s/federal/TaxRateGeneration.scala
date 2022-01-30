package org.kae.ustax4s.federal

import org.scalacheck.Gen

trait TaxRateGeneration:
  val genNonZeroTaxRatePercentage: Gen[Int] =
    Gen.choose(10, 39)

  val genNonZeroTaxRateDouble: Gen[Double] =
    Gen.choose(0.01d, 0.37d)
