package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2017:
  val values: YearlyValues = YearlyValues(
    year = Year.of(2017),
    regime = PreTrump,
    perPersonExemption = Deduction(4050),
    unadjustedStandardDeduction = Map(
      HeadOfHousehold -> 9350,
      Single          -> 6350
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1250),
    adjustmentWhenOver65AndSingle = Deduction(300),
    ordinaryBrackets = Map(
      HeadOfHousehold -> Map(
        0      -> 10d,
        13350  -> 15d,
        50800  -> 25d,
        131200 -> 28d,
        212500 -> 33d,
        416700 -> 35d,
        444550 -> 39.6d
      ),
      Single -> Map(
        0      -> 10d,
        9235   -> 15d,
        37950  -> 25d,
        91900  -> 28d,
        191650 -> 33d,
        416700 -> 35d,
        418400 -> 39.6d
      )
    ).view.mapValues(OrdinaryBrackets.create).toMap,
    qualifiedBrackets = Map(
      HeadOfHousehold -> Map(
        0      -> 0,
        50800  -> 15,
        444550 -> 20
      ),
      Single -> Map(
        0      -> 0,
        37950  -> 15,
        418400 -> 20
      )
    ).view.mapValues(QualifiedBrackets.create).toMap
  )
