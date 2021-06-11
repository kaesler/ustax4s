package org.kae.ustax4s.federal

import scala.math.BigDecimal.RoundingMode
import org.kae.ustax4s.TaxRate

/** Rate of tax payable in a given bracket.
  */
opaque type FederalTaxRate = Double

object FederalTaxRate {
  def unsafeFrom(d: Double): FederalTaxRate =
    require(d >= 0.0)
    require(d <= 0.40)
    d

  given tr: TaxRate[FederalTaxRate] with
    extension (r: FederalTaxRate) def asFraction = r

  given Ordering[FederalTaxRate] with
    def compare(x: FederalTaxRate, y: FederalTaxRate) =
      x.asFraction.compare(y.asFraction)
}
