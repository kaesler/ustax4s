package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2021:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2021),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 25100,
      HeadOfHousehold -> 18800,
      Single          -> 12550
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1350),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 10d,
        19900  -> 12d,
        81050  -> 22d,
        172750 -> 24d,
        329850 -> 32d,
        418850 -> 35d,
        628300 -> 37d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0      -> 10d,
        14200  -> 12d,
        54200  -> 22d,
        86350  -> 24d,
        164900 -> 32d,
        209400 -> 35d,
        523600 -> 37d
      ).asOrdinaryRateFunction,
      Single -> List(
        0      -> 10d,
        9950   -> 12d,
        40525  -> 22d,
        86375  -> 24d,
        164925 -> 32d,
        209425 -> 35d,
        523600 -> 37d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 0d,
        80800  -> 15d,
        501600 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0      -> 0d,
        54100  -> 15d,
        473750 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0      -> 0d,
        40400  -> 15d,
        445850 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2021
