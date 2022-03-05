package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2018:
  val values: YearlyValues = YearlyValues(
    year = Year.of(2018),
    regime = Trump,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      Married         -> 24000,
      HeadOfHousehold -> 18000,
      Single          -> 12000
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1300),
    adjustmentWhenOver65AndSingle = Deduction(300),
    ordinaryBrackets = Map(
      Married -> Map(
        0      -> 10,
        19050  -> 12,
        77400  -> 22,
        165000 -> 24,
        315000 -> 32,
        400000 -> 35,
        600000 -> 37
      ).view.mapValues(_.toDouble).toMap,
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
    ).view.mapValues(OrdinaryBrackets.of).toMap,
    qualifiedBrackets = Map(
      Married -> Map(
        0      -> 0,
        77200  -> 15,
        479000 -> 20
      ),
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
    ).view.mapValues(QualifiedBrackets.of).toMap
  )
