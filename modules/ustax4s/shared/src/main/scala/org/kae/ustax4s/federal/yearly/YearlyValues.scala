package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.money.{Deduction, IncomeThreshold}

final case class YearlyValues(
  year: Year,
  regime: Regime,
  perPersonExemption: Deduction,
  unadjustedStandardDeduction: FilingStatus => Deduction,
  adjustmentWhenOver65: Deduction,
  adjustmentWhenOver65AndSingle: Deduction,
  ordinaryRateFunctions: FilingStatus => OrdinaryRateFunction,
  qualifiedRateFunctions: FilingStatus => QualifiedRateFunction
):
  def previous: Option[YearlyValues] =
    YearlyValues.of(year.minusYears(1L))

  def ordinaryNonZeroThresholdsMap: Map[(FilingStatus, FederalTaxRate), IncomeThreshold] =
    (
      for
        fs                <- FilingStatus.values
        (threshold, rate) <- ordinaryRateFunctions(fs).bracketsAscending
        if threshold != IncomeThreshold.zero
      yield (fs, rate) -> threshold
    ).toMap
  end ordinaryNonZeroThresholdsMap

  def qualifiedNonZeroThresholdsMap: Map[(FilingStatus, FederalTaxRate), IncomeThreshold] =
    (
      for
        fs <- FilingStatus.values
        // brackets          <- qualifiedRateFunctions(fs).toList
        (threshold, rate) <- qualifiedRateFunctions(fs).bracketsAscending
        if threshold != IncomeThreshold.zero
      yield (fs, rate) -> threshold
    ).toMap
  end qualifiedNonZeroThresholdsMap

  def hasCongruentOrdinaryBrackets(that: YearlyValues): Boolean =
    this.ordinaryNonZeroThresholdsMap.keySet == that.ordinaryNonZeroThresholdsMap.keySet

  def hasCongruentQualifiedBrackets(that: YearlyValues): Boolean =
    this.qualifiedNonZeroThresholdsMap.keySet == that.qualifiedNonZeroThresholdsMap.keySet

  lazy val averageThresholdChangeOverPrevious: Option[Double] =
    previous.map(YearlyValues.averageThresholdChange(_, this))

end YearlyValues

object YearlyValues:
  def of(year: Year): Option[YearlyValues] = m.get(year.getValue)

  def first: YearlyValues = m.values.toList.min
  def last: YearlyValues  = m.values.toList.max

  def mostRecentFor(regime: Regime): YearlyValues = m.values
    .filter(_.regime == regime)
    .toList
    .sorted
    .max

  given Ordering[YearlyValues] = Ordering.by(_.year)

  def averageThresholdChangeOverPrevious(year: Year): Option[Double] =
    memoizedAverageThresholdChanges.get(year)

  private def averageThresholdChange(
    left: YearlyValues,
    right: YearlyValues
  ): Double =
    val pairs: List[(IncomeThreshold, IncomeThreshold)] =
      (if left.hasCongruentOrdinaryBrackets(right) then
         left.ordinaryNonZeroThresholdsMap.values.toList.sorted
           .zip(right.ordinaryNonZeroThresholdsMap.values.toList.sorted)
       else List.empty) ++
        left.qualifiedNonZeroThresholdsMap.values.toList.sorted
          .zip(right.qualifiedNonZeroThresholdsMap.values.toList.sorted)
    pairs.map((earlier, later) => later div earlier).sum / pairs.length
  end averageThresholdChange

  private lazy val m: Map[Int, YearlyValues] = Map(
    2016 -> Year2016.values,
    2017 -> Year2017.values,
    2018 -> Year2018.values,
    2019 -> Year2019.values,
    2020 -> Year2020.values,
    2021 -> Year2021.values,
    2022 -> Year2022.values,
    2023 -> Year2023.values,
    2024 -> Year2024.values,
    2025 -> Year2025.values,
    2026 -> Year2026.values
  )

  private lazy val memoizedAverageThresholdChanges: Map[Year, Double] =
    (for
      values <- m.values.toList
      change <- values.averageThresholdChangeOverPrevious.toList
    yield values.year -> change).toMap

end YearlyValues
