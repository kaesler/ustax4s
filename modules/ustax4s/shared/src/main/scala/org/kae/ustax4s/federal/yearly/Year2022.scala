package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2022:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2022),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 25900,
      HeadOfHousehold -> 19400,
      Single          -> 12950
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1400),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 10d,
        20550  -> 12d,
        83550  -> 22d,
        178150 -> 24d,
        340100 -> 32d,
        431900 -> 35d,
        647850 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0      -> 10d,
        14650  -> 12d,
        55900  -> 22d,
        89050  -> 24d,
        170050 -> 32d,
        215950 -> 35d,
        539900 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0      -> 10d,
        10275  -> 12d,
        41775  -> 22d,
        89075  -> 24d,
        170050 -> 32d,
        215950 -> 35d,
        539900 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 0d,
        83350  -> 15d,
        517200 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0      -> 0d,
        55800  -> 15d,
        488500 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0      -> 0d,
        41675  -> 15d,
        459750 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2022
