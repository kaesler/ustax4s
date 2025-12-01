package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2025:
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
      MarriedJoint -> Map(
        0      -> 10,
        23850  -> 12,
        96950  -> 22,
        206700 -> 24,
        394600 -> 32,
        501050 -> 35,
        751600 -> 37
      ).view.mapValues(_.toDouble).toMap,
      HeadOfHousehold -> Map(
        0      -> 10,
        17000  -> 12,
        64850  -> 22,
        103350 -> 24,
        197300 -> 32,
        250500 -> 35,
        626350 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0      -> 10,
        11925  -> 12,
        48475  -> 22,
        103350 -> 24,
        197300 -> 32,
        250525 -> 35,
        626350 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryRateFunction.of).toMap,
    qualifiedRateFunctions = Map(
      MarriedJoint -> Map(
        0      -> 0,
        96700  -> 15,
        600050 -> 20
      ),
      HeadOfHousehold -> Map(
        0      -> 0,
        64750  -> 15,
        566700 -> 20
      ),
      Single -> Map(
        0      -> 0,
        48350  -> 15,
        533400 -> 20
      )
    ).view.mapValues(QualifiedRateFunction.of).toMap
  )
end Year2025
