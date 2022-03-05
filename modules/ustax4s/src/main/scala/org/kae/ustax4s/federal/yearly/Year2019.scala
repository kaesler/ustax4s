package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2019:
  val values: YearlyValues = YearlyValues(
    year = Year.of(2019),
    regime = Trump,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = Map(
      Married         -> 24400,
      HeadOfHousehold -> 18350,
      Single          -> 12200
    ).view.mapValues(Deduction.apply),
    adjustmentWhenOver65 = Deduction(1300),
    adjustmentWhenOver65AndSingle = Deduction(350),
    ordinaryBrackets = Map(
      Married -> Map(
        0      -> 10,
        19400  -> 12,
        78950  -> 22,
        168400 -> 24,
        321450 -> 32,
        408200 -> 35,
        612350 -> 37
      ).view.mapValues(_.toDouble).toMap,
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
    ).view.mapValues(OrdinaryBrackets.of).toMap,
    qualifiedBrackets = Map(
      Married -> Map(
        0      -> 0,
        78750  -> 15,
        488850 -> 20
      ),
      HeadOfHousehold -> Map(
        0      -> 0,
        52750  -> 15,
        461700 -> 20
      ),
      Single -> Map(
        0      -> 0,
        39375  -> 15,
        434550 -> 20
      )
    ).view.mapValues(QualifiedBrackets.of).toMap
  )
