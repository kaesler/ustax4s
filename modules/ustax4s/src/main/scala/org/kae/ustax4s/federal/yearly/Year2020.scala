package org.kae.ustax4s.federal
package yearly

import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2020:
  val values: YearlyValues = YearlyValues(
    regime = Trump,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      HeadOfHousehold -> 18650,
      Single          -> 12400
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1300),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryBrackets = Map(
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
    ).view.mapValues(OrdinaryBrackets.create),
    qualifiedBrackets = Map(
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
    ).view.mapValues(QualifiedBrackets.create)
  )
