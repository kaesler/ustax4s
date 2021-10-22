package org.kae.ustax4s
import java.time.Year

// Used to estimate future tax regimes.
final case class Inflation(
  targetFutureYear: Year,
  // E.g. 0.02 for 2%
  annualGrowthFraction: Double
):
  require(targetFutureYear.getValue > 2017)
  require(annualGrowthFraction >= 0)

  // Compute the inflation factor for the target year, from a base year.
  def factor(baseYear: Year): Double =
    import math.Ordered.orderingToOrdered
    require(targetFutureYear >= baseYear)
    math.pow(
      1 + annualGrowthFraction,
      targetFutureYear.getValue - baseYear.getValue
    )
