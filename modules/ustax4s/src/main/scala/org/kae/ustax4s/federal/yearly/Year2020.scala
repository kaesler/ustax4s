package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2020:
  val values: YearlyValues = YearlyValues(
    year = Year.of(2020),
    regime = TCJA,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      Married         -> 24800,
      HeadOfHousehold -> 18650,
      Single          -> 12400
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1300),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryBrackets = Map(
      Married -> Map(
        0      -> 10,
        19750  -> 12,
        80250  -> 22,
        171050 -> 24,
        326600 -> 32,
        414700 -> 35,
        622050 -> 37
      ).view.mapValues(_.toDouble).toMap,
      HeadOfHousehold -> Map(
        0      -> 10,
        14100  -> 12,
        53700  -> 22,
        85500  -> 24,
        163300 -> 32,
        207350 -> 35,
        518400 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0      -> 10,
        9875   -> 12,
        40125  -> 22,
        85525  -> 24,
        163300 -> 32,
        207350 -> 35,
        518400 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryBrackets.of).toMap,
    qualifiedBrackets = Map(
      Married -> Map(
        0      -> 0,
        80000  -> 15,
        496600 -> 20
      ),
      HeadOfHousehold -> Map(
        0      -> 0,
        53600  -> 15,
        469050 -> 20
      ),
      Single -> Map(
        0      -> 0,
        40000  -> 15,
        442450 -> 20
      )
    ).view.mapValues(QualifiedBrackets.of).toMap
  )
