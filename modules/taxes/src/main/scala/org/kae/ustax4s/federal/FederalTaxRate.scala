package org.kae.ustax4s.federal

import cats.kernel.Order
import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.types.numeric.{NonNegBigDecimal, PosDouble, PosInt}
import scala.language.implicitConversions
import scala.math.BigDecimal.RoundingMode

/** Rate of tax payable in a given bracket.
  */
type FederalTaxRateRefinement = Interval.Closed[0.0d, 0.37d]
type FederalTaxRate           = Double Refined FederalTaxRateRefinement

object FederalTaxRate {

  def unsafeFrom(d: Double): FederalTaxRate =
    refineV[FederalTaxRateRefinement](d).toOption.get
}

// TODO: Use Scala3?  Type class?
given Ordering[FederalTaxRate] = Ordering.by(_.value)
// TODO: Use Scala3?  Type class?

implicit def orderedForTaxRate(tr: FederalTaxRate): Ordered[FederalTaxRate] =
  Ordered.orderingToOrdered(tr)
