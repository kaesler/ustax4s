package org.kae.ustax4s.state

import cats.kernel.Order
import org.kae.ustax4s.TaxRate
import scala.math.BigDecimal.RoundingMode

/** Rate of tax payable in a given bracket.
  */
opaque type StateTaxRate = Double

object StateTaxRate {

  def unsafeFrom(d: Double): StateTaxRate =
    require(d >= 0.0)
    require(d <= 0.10)
    d

  given tr: TaxRate[StateTaxRate] with
    extension (r: StateTaxRate) def asFraction = r

  given Ordering[StateTaxRate] with
    def compare(x: StateTaxRate, y: StateTaxRate) =
      x.asFraction.compare(y.asFraction)
}
