package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2024:
  import Syntax.*

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
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 10d,
        23200  -> 12d,
        94300  -> 22d,
        201050 -> 24d,
        383900 -> 32d,
        487450 -> 35d,
        731200 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0      -> 10d,
        16550  -> 12d,
        63100  -> 22d,
        100500 -> 24d,
        191950 -> 32d,
        243700 -> 35d,
        609350 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0      -> 10d,
        11600  -> 12d,
        47150  -> 22d,
        100525 -> 24d,
        191950 -> 32d,
        243725 -> 35d,
        609350 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 0d,
        94050  -> 15d,
        583750 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0      -> 0d,
        63000  -> 15d,
        551350 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0      -> 0d,
        47025  -> 15d,
        518900 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2024
