package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2023:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2023),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 27700,
      HeadOfHousehold -> 20800,
      Single          -> 13850
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1500),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 10d,
        22000  -> 12d,
        89450  -> 22d,
        190750 -> 24d,
        364200 -> 32d,
        462500 -> 35d,
        693750 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0      -> 10d,
        15700  -> 12d,
        59850  -> 22d,
        95350  -> 24d,
        182100 -> 32d,
        231250 -> 35d,
        578100 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0      -> 10d,
        11000  -> 12d,
        44725  -> 22d,
        95375  -> 24d,
        182100 -> 32d,
        231250 -> 35d,
        578125 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 0d,
        89250  -> 15d,
        553850 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0      -> 0d,
        59750  -> 15d,
        523050 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0      -> 0d,
        44625  -> 15d,
        492300 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2023
