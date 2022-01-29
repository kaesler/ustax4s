package org.kae.ustax4s.federal
package yearly

import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2022:
  val values: YearlyValues = YearlyValues(
    regime = Trump,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      HeadOfHousehold -> 19400,
      Single          -> 12950
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1400),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryBrackets = Map(
      HeadOfHousehold -> Map(
        0      -> 10,
        14650  -> 12,
        55900  -> 22,
        89050  -> 24,
        170050 -> 32,
        215950 -> 35,
        539900 -> 37
      ).view.mapValues(_.toDouble).toMap,
      Single -> Map(
        0      -> 10,
        10275  -> 12,
        41775  -> 22,
        89075  -> 24,
        170050 -> 32,
        215950 -> 35,
        539900 -> 37
      ).view.mapValues(_.toDouble).toMap
    ).view.mapValues(OrdinaryBrackets.create),
    qualifiedBrackets = Map(
      HeadOfHousehold -> Map(
      ),
      Single -> Map(
      )
    ).view.mapValues(QualifiedBrackets.create)
  )
