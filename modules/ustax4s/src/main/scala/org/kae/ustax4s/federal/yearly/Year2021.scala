package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2021:
  val values: YearlyValues = YearlyValues(
    year = Year.of(2021),
    regime = Trump,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      HeadOfHousehold -> 18800,
      Single          -> 12550
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1350),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryBrackets = Map(
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
