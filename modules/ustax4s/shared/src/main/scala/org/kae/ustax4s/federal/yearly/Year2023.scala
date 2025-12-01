package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2023:
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
      MarriedJoint -> Map(
        0      -> 10,
        22000  -> 12,
        89450  -> 22,
        190750 -> 24,
        364200 -> 32,
        462500 -> 35,
        693750 -> 37
      ).view.mapValues(_.toDouble).toMap,
      HeadOfHousehold -> Map(
        0      -> 10,
        15700  -> 12,
        59850  -> 22,
        95350  -> 24,
        182100 -> 32,
        231250 -> 35,
        578100 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0      -> 10,
        11000  -> 12,
        44725  -> 22,
        95375  -> 24,
        182100 -> 32,
        231250 -> 35,
        578125 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryRateFunction.of).toMap,
    qualifiedRateFunctions = Map(
      MarriedJoint -> Map(
        0      -> 0,
        89250  -> 15,
        553850 -> 20
      ),
      HeadOfHousehold -> Map(
        0      -> 0,
        59750  -> 15,
        523050 -> 20
      ),
      Single -> Map(
        0      -> 0,
        44625  -> 15,
        492300 -> 20
      )
    ).view.mapValues(QualifiedRateFunction.of).toMap
  )
end Year2023
