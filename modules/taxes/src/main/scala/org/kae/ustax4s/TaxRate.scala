package org.kae.ustax4s

import cats.kernel.Order
import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.types.numeric.{NonNegBigDecimal, PosDouble, PosInt}
import scala.language.implicitConversions
import scala.math.BigDecimal.RoundingMode

/** Rate of tax payable in a given bracket.
  */
type TaxRateRefinement = Interval.Closed[0.0d, 0.37d]
type TaxRate           = Double Refined TaxRateRefinement

object TaxRate {

  def unsafeFrom(d: Double): TaxRate =
    refineV[TaxRateRefinement](d).toOption.get
}
// TODO: Use Scala3?  Type class?
given Ordering[TaxRate] = Ordering.by(_.value)
// TODO: Use Scala3?  Type class?

implicit def orderedForTaxRate(tr: TaxRate): Ordered[TaxRate] =
  Ordered.orderingToOrdered(tr)
