package org.kae.ustax4s.federal

import org.kae.ustax4s.TaxRate
import scala.math.BigDecimal.RoundingMode

/** Rate of tax payable in a given bracket.
  */
opaque type FederalTaxRate = Double

object FederalTaxRate:

  val zero: FederalTaxRate = 0.0

  def unsafeFrom(d: Double): FederalTaxRate =
    require(d >= 0.0)
    require(d <= 0.40)
    d

  given tr: TaxRate[FederalTaxRate] with
    extension (left: FederalTaxRate)
      def asFraction: Double = left

      infix def absoluteDifference(right: FederalTaxRate): FederalTaxRate =
        (left - right).abs
    end extension

  given Ordering[FederalTaxRate] with
    def compare(x: FederalTaxRate, y: FederalTaxRate): Int =
      x.asFraction.compare(y.asFraction)

end FederalTaxRate
