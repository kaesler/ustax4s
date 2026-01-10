package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2020:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2020),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 24800,
      HeadOfHousehold -> 18650,
      Single          -> 12400
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1300),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 10d,
        19750  -> 12d,
        80250  -> 22d,
        171050 -> 24d,
        326600 -> 32d,
        414700 -> 35d,
        622050 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0      -> 10d,
        14100  -> 12d,
        53700  -> 22d,
        85500  -> 24d,
        163300 -> 32d,
        207350 -> 35d,
        518400 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0      -> 10d,
        9875   -> 12d,
        40125  -> 22d,
        85525  -> 24d,
        163300 -> 32d,
        207350 -> 35d,
        518400 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 0d,
        80000  -> 15d,
        496600 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0      -> 0d,
        53600  -> 15d,
        469050 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0      -> 0d,
        40000  -> 15d,
        442450 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2020
