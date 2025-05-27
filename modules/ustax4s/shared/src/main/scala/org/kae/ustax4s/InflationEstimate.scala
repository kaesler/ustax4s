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
  require(targetFutureYear > YearlyValues.last.year, SourceLoc.loc)
  require(annualGrowthFraction >= 0, SourceLoc.loc)

  // Compute the inflation factor for the target year, from a base year.
  def factor(baseYear: Year): Double =
    require(targetFutureYear >= baseYear, SourceLoc.loc)
    math.pow(
      1 + annualGrowthFraction,
      targetFutureYear.getValue - baseYear.getValue
    )
