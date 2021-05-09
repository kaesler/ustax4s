package org.kae.ustax4s

import org.scalacheck.Gen

trait TaxRateGeneration {
  val genNonZeroTaxRate: Gen[TaxRate] =
    Gen.choose(0.01d, 0.37d).map(TaxRate.unsafeFrom)
}
