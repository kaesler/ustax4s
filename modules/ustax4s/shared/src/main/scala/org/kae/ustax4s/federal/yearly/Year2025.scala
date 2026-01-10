package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2025:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2025),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 31500,
      HeadOfHousehold -> 23625,
      Single          -> 15750
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1600),
    adjustmentWhenOver65AndSingle = Deduction(400),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 10d,
        23850  -> 12d,
        96950  -> 22d,
        206700 -> 24d,
        394600 -> 32d,
        501050 -> 35d,
        751600 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0      -> 10d,
        17000  -> 12d,
        64850  -> 22d,
        103350 -> 24d,
        197300 -> 32d,
        250500 -> 35d,
        626350 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0      -> 10d,
        11925  -> 12d,
        48475  -> 22d,
        103350 -> 24d,
        197300 -> 32d,
        250525 -> 35d,
        626350 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 0d,
        96700  -> 15d,
        600050 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0      -> 0d,
        64750  -> 15d,
        566700 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0      -> 0d,
        48350  -> 15d,
        533400 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2025
