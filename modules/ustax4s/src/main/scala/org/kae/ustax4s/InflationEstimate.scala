package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.federal.yearly.YearlyValues
import scala.math.Ordered.orderingToOrdered

// Used to estimate future tax brackets, exemptions, deductions etc.
final case class InflationEstimate(
  targetFutureYear: Year,
  // E.g. 0.02 for 2%
  annualGrowthFraction: Double
):
  require(targetFutureYear > YearlyValues.last.year)
  require(annualGrowthFraction >= 0)

  // Compute the inflation factor for the target year, from a base year.
  def factor(baseYear: Year): Double =
    import math.Ordered.orderingToOrdered
    require(targetFutureYear >= baseYear)
    math.pow(
      1 + annualGrowthFraction,
      targetFutureYear.getValue - baseYear.getValue
    )
