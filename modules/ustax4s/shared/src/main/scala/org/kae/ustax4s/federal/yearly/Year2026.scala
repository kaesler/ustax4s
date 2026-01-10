package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2026:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2026),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 32_200,
      HeadOfHousehold -> 24_175,
      Single          -> 16_100
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1_650),
    adjustmentWhenOver65AndSingle = Deduction(400),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0       -> 10d,
        24_800  -> 12d,
        100_800 -> 22d,
        211_400 -> 24d,
        403_550 -> 32d,
        512_450 -> 35d,
        768_700 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0       -> 10d,
        17_700  -> 12d,
        67_450  -> 22d,
        105_700 -> 24d,
        201_750 -> 32d,
        256_200 -> 35d,
        640_600 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0       -> 10d,
        12_400  -> 12d,
        50_400  -> 22d,
        105_700 -> 24d,
        201_775 -> 32d,
        256_225 -> 35d,
        640_600 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0       -> 0d,
        98_900  -> 15d,
        613_700 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0       -> 0d,
        66_200  -> 15d,
        579_600 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0       -> 0d,
        49_450  -> 15d,
        545_500 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2026
