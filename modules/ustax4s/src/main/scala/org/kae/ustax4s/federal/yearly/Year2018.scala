package org.kae.ustax4s.federal
package yearly

import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2018:
  val values: YearlyValues = YearlyValues(
    regime = Trump,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      HeadOfHousehold -> 18000,
      Single          -> 12000
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1300),
    adjustmentWhenOver65AndSingle = Deduction(300),
    ordinaryBrackets = Map(
      HeadOfHousehold -> Map(
        0      -> 10,
        13600  -> 12,
        51800  -> 22,
        82500  -> 24,
        157500 -> 32,
        200000 -> 35,
        500000 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0      -> 10,
        9525   -> 12,
        38700  -> 22,
        82500  -> 24,
        157500 -> 32,
        200000 -> 35,
        500000 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryBrackets.create),
    qualifiedBrackets = Map(
      HeadOfHousehold -> Map(
        0      -> 0,
        51700  -> 15,
        452400 -> 20
      ),
      Single -> Map(
        0      -> 0,
        38600  -> 15,
        425800 -> 20
      )
    ).view.mapValues(QualifiedBrackets.create)
  )
