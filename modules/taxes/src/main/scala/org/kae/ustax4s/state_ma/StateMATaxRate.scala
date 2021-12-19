package org.kae.ustax4s.state_ma

import cats.kernel.Order
import org.kae.ustax4s.TaxRate
import scala.math.BigDecimal.RoundingMode

/** Rate of tax payable in a given bracket.
  */
opaque type StateMATaxRate = Double

object StateMATaxRate:

  def unsafeFrom(d: Double): StateMATaxRate =
    require(d >= 0.0)
    require(d <= 0.10)
    d

  given tr: TaxRate[StateMATaxRate] with
    extension (left: StateMATaxRate)
      def asFraction = left
      infix def absoluteDifference(right: StateMATaxRate): StateMATaxRate =
        (left - right).abs

  given Ordering[StateMATaxRate] with
    def compare(x: StateMATaxRate, y: StateMATaxRate): Int =
      x.asFraction.compare(y.asFraction)

end StateMATaxRate
