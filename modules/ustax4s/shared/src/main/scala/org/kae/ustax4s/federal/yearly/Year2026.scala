package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2026:
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
    ordinaryBrackets = Map(
      MarriedJoint -> Map(
        0       -> 10,
        24_800  -> 12,
        100_800 -> 22,
        211_400 -> 24,
        403_550 -> 32,
        512_450 -> 35,
        768_700 -> 37
      ).view.mapValues(_.toDouble).toMap,
      HeadOfHousehold -> Map(
        0       -> 10,
        17_700  -> 12,
        67_450  -> 22,
        105_700 -> 24,
        201_750 -> 32,
        256_200 -> 35,
        640_600 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0       -> 10,
        12_400  -> 12,
        50_400  -> 22,
        105_700 -> 24,
        201_775 -> 32,
        256_225 -> 35,
        640_600 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryBrackets.of).toMap,
    qualifiedBrackets = Map(
      MarriedJoint -> Map(
        0       -> 0,
        98_900  -> 15,
        613_700 -> 20
      ),
      HeadOfHousehold -> Map(
        0       -> 0,
        66_200  -> 15,
        579_600 -> 20
      ),
      Single -> Map(
        0       -> 0,
        49_450  -> 15,
        545_500 -> 20
      )
    ).view.mapValues(QualifiedBrackets.of).toMap
  )
end Year2026
