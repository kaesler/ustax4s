package org.kae.ustax4s.federal
import org.scalacheck.Gen

trait TaxRateGeneration:
  val genNonZeroTaxRate: Gen[FederalTaxRate] =
    Gen.choose(0.01d, 0.37d).map(FederalTaxRate.unsafeFrom)
