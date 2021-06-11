package org.kae.ustax4s.state

import cats.kernel.Order
import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.types.numeric.{NonNegBigDecimal, PosDouble, PosInt}
import org.kae.ustax4s.TaxRate
import scala.language.implicitConversions
import scala.math.BigDecimal.RoundingMode

/** Rate of tax payable in a given bracket.
  */
private[state] type StateTaxRateRefinement = Interval.Closed[0.0d, 0.10d]
type StateTaxRate                          = Double Refined StateTaxRateRefinement

object StateTaxRate {

  def unsafeFrom(d: Double): StateTaxRate =
    refineV[StateTaxRateRefinement](d).toOption.get

  given tr: TaxRate[StateTaxRate] with
    extension (r: StateTaxRate) def asFraction = r.value
}
given Ordering[StateTaxRate] = StateTaxRate.tr

implicit def orderedForTaxRate(tr: StateTaxRate): Ordered[StateTaxRate] =
  Ordered.orderingToOrdered(tr)
