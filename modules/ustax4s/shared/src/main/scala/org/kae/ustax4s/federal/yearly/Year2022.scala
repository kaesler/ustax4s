package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2022:
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
      MarriedJoint -> Map(
        0      -> 10,
        20550  -> 12,
        83550  -> 22,
        178150 -> 24,
        340100 -> 32,
        431900 -> 35,
        647850 -> 37
      ).view.mapValues(_.toDouble).toMap,
      HeadOfHousehold -> Map(
        0      -> 10,
        14650  -> 12,
        55900  -> 22,
        89050  -> 24,
        170050 -> 32,
        215950 -> 35,
        539900 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0      -> 10,
        10275  -> 12,
        41775  -> 22,
        89075  -> 24,
        170050 -> 32,
        215950 -> 35,
        539900 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryRateFunction.of).toMap,
    qualifiedRateFunctions = Map(
      MarriedJoint -> Map(
        0      -> 0,
        83350  -> 15,
        517200 -> 20
      ),
      HeadOfHousehold -> Map(
        0      -> 0,
        55800  -> 15,
        488500 -> 20
      ),
      Single -> Map(
        0      -> 0,
        41675  -> 15,
        459750 -> 20
      )
    ).view.mapValues(QualifiedRateFunction.of).toMap
  )
end Year2022
