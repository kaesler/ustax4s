package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2018:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2018),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction =
      Map(MarriedJoint -> 24000, HeadOfHousehold -> 18000, Single -> 12000).view
        .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1300),
    adjustmentWhenOver65AndSingle = Deduction(300),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 10d,
        19050  -> 12d,
        77400  -> 22d,
        165000 -> 24d,
        315000 -> 32d,
        400000 -> 35d,
        600000 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0      -> 10d,
        13600  -> 12d,
        51800  -> 22d,
        82500  -> 24d,
        157500 -> 32d,
        200000 -> 35d,
        500000 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0      -> 10d,
        9525   -> 12d,
        38700  -> 22d,
        82500  -> 24d,
        157500 -> 32d,
        200000 -> 35d,
        500000 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 0d,
        77200  -> 15d,
        479000 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0      -> 0d,
        51700  -> 15d,
        452400 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0      -> 0d,
        38600  -> 15d,
        425800 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2018
