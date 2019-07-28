package org.kae.ustax4s

import org.scalacheck.Gen

trait TaxRateGeneration {
  val genTaxRate: Gen[TaxRate] =
    Gen.choose(0.01D, 0.37D).map(TaxRate.unsafeFrom)
}
