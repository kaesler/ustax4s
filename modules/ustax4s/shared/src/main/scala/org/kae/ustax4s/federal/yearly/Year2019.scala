package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2019:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2019),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 24400,
      HeadOfHousehold -> 18350,
      Single          -> 12200
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1300),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 10d,
        19400  -> 12d,
        78950  -> 22d,
        168400 -> 24d,
        321450 -> 32d,
        408200 -> 35d,
        612350 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0      -> 10d,
        13850  -> 12d,
        52850  -> 22d,
        84200  -> 24d,
        160700 -> 32d,
        204100 -> 35d,
        510300 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0      -> 10d,
        9700   -> 12d,
        39475  -> 22d,
        84200  -> 24d,
        160725 -> 32d,
        204100 -> 35d,
        510300 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 0d,
        78750  -> 15d,
        488850 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0      -> 0d,
        52750  -> 15d,
        461700 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0      -> 0d,
        39375  -> 15d,
        434550 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2019
