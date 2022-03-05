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
      Married         -> 12700,
      HeadOfHousehold -> 9350,
      Single          -> 6350
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1250),
    adjustmentWhenOver65AndSingle = Deduction(300),
    ordinaryBrackets = Map(
      Married -> Map(
        0      -> 10d,
        18650  -> 15d,
        75900  -> 25d,
        153100 -> 28d,
        233350 -> 33d,
        416700 -> 35d,
        470700 -> 39.6d
      ),
      HeadOfHousehold -> Map(
        0      -> 10d,
        13350  -> 15d,
        50800  -> 25d,  //
        131200 -> 28d,
        212500 -> 33d,
        416700 -> 35d,
        444550 -> 39.6d //
      ),
      Single -> Map(
        0      -> 10d,
        9325   -> 15d,
        37950  -> 25d,
        91900  -> 28d,
        191650 -> 33d,
        416700 -> 35d,
        418400 -> 39.6d
      )
    ).view.mapValues(OrdinaryBrackets.of).toMap,
    qualifiedBrackets = Map(
      Married -> Map(
        0      -> 0,
        75900  -> 15,
        470700 -> 20
      ),
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
    ).view.mapValues(QualifiedBrackets.of).toMap
  )
