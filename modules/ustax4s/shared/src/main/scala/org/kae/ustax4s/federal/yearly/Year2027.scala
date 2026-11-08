package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2027:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2027),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 33_200,
      HeadOfHousehold -> 24_925,
      Single          -> 16_600
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1_700),
    adjustmentWhenOver65AndSingle = Deduction(400),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0       -> 10d,
        25_600  -> 12d,
        104_050 -> 22d,
        218_250 -> 24d,
        416_650 -> 32d,
        529_100 -> 35d,
        793_650 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0       -> 10d,
        18_250  -> 12d,
        69_650  -> 22d,
        109_100 -> 24d,
        208_300 -> 32d,
        264_550 -> 35d,
        661_350 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0       -> 10d,
        12_800  -> 12d,
        52_025  -> 22d,
        109_125 -> 24d,
        208_325 -> 32d,
        264_550 -> 35d,
        661_375 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0       -> 0d,
        102_100  -> 15d,
        633_600 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0       -> 0d,
        68_350  -> 15d,
        598_400 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0       -> 0d,
        51_050  -> 15d,
        563_200 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2027
