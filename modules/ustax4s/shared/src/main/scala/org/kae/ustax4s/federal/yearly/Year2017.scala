package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2017:
  import Syntax.*

  val values: YearlyValues = YearlyValues(
    year = Year.of(2017),
    regime = PreTCJA,
    perPersonExemption = Deduction(4050),
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 12700,
      HeadOfHousehold -> 9350,
      Single          -> 6350
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1250),
    adjustmentWhenOver65AndSingle = Deduction(300),
    ordinaryRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 10d,
        18650  -> 15d,
        75900  -> 25d,
        153100 -> 28d,
        233350 -> 33d,
        416700 -> 35d,
        470700 -> 39.6d
      ).asOrdinaryRateFunction,
      HeadOfHousehold -> List(
        0      -> 10d,
        13350  -> 15d,
        50800  -> 25d,  //
        131200 -> 28d,
        212500 -> 33d,
        416700 -> 35d,
        444550 -> 39.6d //
      ).asOrdinaryRateFunction,
      Single -> List(
        0      -> 10d,
        9325   -> 15d,
        37950  -> 25d,
        91900  -> 28d,
        191650 -> 33d,
        416700 -> 35d,
        418400 -> 39.6d
      ).asOrdinaryRateFunction
    ),
    qualifiedRateFunctions = Map(
      MarriedJoint -> List(
        0      -> 0d,
        75900  -> 15d,
        470700 -> 20d
      ).asQualifiedRateFunction,
      HeadOfHousehold -> List(
        0      -> 0d,
        50800  -> 15d,
        444550 -> 20d
      ).asQualifiedRateFunction,
      Single -> List(
        0      -> 0d,
        37950  -> 15d,
        418400 -> 20d
      ).asQualifiedRateFunction
    )
  )
end Year2017
