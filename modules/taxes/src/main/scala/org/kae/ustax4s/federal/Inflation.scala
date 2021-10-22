package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.Year
import math.Ordered.orderingToOrdered

final case class Inflation(
  targetYear: Year,
  // E.g. 0.02 for 2%
  annualGrowthFraction: Double
) {
  require(targetYear.getValue > 2017)
  require(annualGrowthFraction >= 0)

  def factor(baseYear: Year): Double =
    require(targetYear >= baseYear)
    math.pow(1 + annualGrowthFraction, targetYear.getValue - baseYear.getValue)
}
