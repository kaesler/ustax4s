package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2024:
  val values: YearlyValues = YearlyValues(
    year = Year.of(2024),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 29200,
      HeadOfHousehold -> 21900,
      Single          -> 14600
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1550),
    adjustmentWhenOver65AndSingle = Deduction(400),
    ordinaryBrackets = Map(
      MarriedJoint -> Map(
        0      -> 10,
        23200  -> 12,
        94300  -> 22,
        201050 -> 24,
        383900 -> 32,
        487450 -> 35,
        731200 -> 37
      ).view.mapValues(_.toDouble).toMap,
      HeadOfHousehold -> Map(
        0      -> 10,
        16550  -> 12,
        63100  -> 22,
        100500 -> 24,
        191950 -> 32,
        243700 -> 35,
        609350 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0      -> 10,
        11600  -> 12,
        47150  -> 22,
        100525 -> 24,
        191950 -> 32,
        243725 -> 35,
        609350 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryBrackets.of).toMap,
    qualifiedBrackets = Map(
      MarriedJoint -> Map(
        0      -> 0,
        94050  -> 15,
        583750 -> 20
      ),
      HeadOfHousehold -> Map(
        0      -> 0,
        63000  -> 15,
        551350 -> 20
      ),
      Single -> Map(
        0      -> 0,
        47025  -> 15,
        518900 -> 20
      )
    ).view.mapValues(QualifiedBrackets.of).toMap
  )
end Year2024
