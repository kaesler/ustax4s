package org.kae.ustax4s.federal
package yearly

import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2019:
  val values: YearlyValues = YearlyValues(
    regime = Trump,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      HeadOfHousehold -> 18350,
      Single          -> 12200
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1300),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryBrackets = Map(
      HeadOfHousehold -> Map(
        0      -> 10,
        13850  -> 12,
        52850  -> 22,
        84200  -> 24,
        160700 -> 32,
        204100 -> 35,
        510300 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0      -> 10,
        9700   -> 12,
        39475  -> 22,
        84200  -> 24,
        160725 -> 32,
        204100 -> 35,
        510300 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryBrackets.create),
    qualifiedBrackets = Map(
      HeadOfHousehold -> Map(
      ),
      Single -> Map(
      )
    ).view.mapValues(QualifiedBrackets.create)
  )
