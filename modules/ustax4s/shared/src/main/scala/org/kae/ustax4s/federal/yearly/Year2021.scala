package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2021:
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
    ordinaryBrackets = Map(
      MarriedJoint -> Map(
        0      -> 10,
        19900  -> 12,
        81050  -> 22,
        172750 -> 24,
        329850 -> 32,
        418850 -> 35,
        628300 -> 37
      ).view.mapValues(_.toDouble).toMap,
      HeadOfHousehold -> Map(
        0      -> 10,
        14200  -> 12,
        54200  -> 22,
        86350  -> 24,
        164900 -> 32,
        209400 -> 35,
        523600 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0      -> 10,
        9950   -> 12,
        40525  -> 22,
        86375  -> 24,
        164925 -> 32,
        209425 -> 35,
        523600 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryBrackets.of).toMap,
    qualifiedBrackets = Map(
      MarriedJoint -> Map(
        0      -> 0,
        80800  -> 15,
        501600 -> 20
      ),
      HeadOfHousehold -> Map(
        0      -> 0,
        54100  -> 15,
        473750 -> 20
      ),
      Single -> Map(
        0      -> 0,
        40400  -> 15,
        445850 -> 20
      )
    ).view.mapValues(QualifiedBrackets.of).toMap
  )
end Year2021
