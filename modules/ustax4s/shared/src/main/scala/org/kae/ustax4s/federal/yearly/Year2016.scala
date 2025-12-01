package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2016:
  val values: YearlyValues = YearlyValues(
    year = Year.of(2016),
    regime = PreTCJA,
    perPersonExemption = Deduction(4050),
    unadjustedStandardDeduction = Map(
      MarriedJoint    -> 12600,
      HeadOfHousehold -> 9300,
      Single          -> 6300
    ).view
      .mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1250),
    adjustmentWhenOver65AndSingle = Deduction(300),
    ordinaryRateFunctions = Map(
      MarriedJoint -> Map(
        0      -> 10d,
        18550  -> 15d,
        75300  -> 25d,
        151900 -> 28d,
        231450 -> 33d,
        413350 -> 35d,
        466950 -> 39.6d
      ),
      HeadOfHousehold -> Map(
        0      -> 10d,
        13250  -> 15d,
        50400  -> 25d,  //
        130150 -> 28d,
        210800 -> 33d,
        413350 -> 35d,
        441000 -> 39.6d //
      ),
      Single -> Map(
        0      -> 10d,
        9275   -> 15d,
        37650  -> 25d,
        91150  -> 28d,
        190150 -> 33d,
        413350 -> 35d,
        415050 -> 39.6d
      )
    ).view.mapValues(OrdinaryRateFunction.of).toMap,
    qualifiedRateFunctions = Map(
      MarriedJoint -> Map(
        0      -> 0,
        75300  -> 15,
        466950 -> 20
      ),
      HeadOfHousehold -> Map(
        0      -> 0,
        50400  -> 15,
        441000 -> 20
      ),
      Single -> Map(
        0      -> 0,
        37650  -> 15,
        415050 -> 20
      )
    ).view.mapValues(QualifiedRateFunction.of).toMap
  )
end Year2016
