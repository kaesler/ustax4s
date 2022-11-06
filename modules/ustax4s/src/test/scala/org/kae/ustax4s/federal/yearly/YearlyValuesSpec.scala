package org.kae.ustax4s.federal
package yearly

import cats.implicits.*
import java.time.Year
import munit.{Assertions, FunSuite}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.{Deduction, Income, IncomeThreshold}
import scala.math.Ordering.Implicits.infixOrderingOps

// Tests that the static data encoding yearly values is correctly entered.
class YearlyValuesSpec extends FunSuite:
  private val allYears = List(
    Year2016.values,
    Year2017.values,
    Year2018.values,
    Year2019.values,
    Year2020.values,
    Year2021.values,
    Year2022.values
  ).sortBy(_.year)

  private val preTCJAYears = allYears.filter(_.regime == PreTCJA)
  private val trumpYears    = allYears.filter(_.regime == TCJA)

  private def massert(
    b: Boolean,
    clue: => Any = "assertion failed"
  ): Unit = Assertions.assert(b, clue)

  test("No duplicate years") {
    assertEquals(
      allYears.map(_.year.getValue).distinct.size,
      allYears.size
    )
  }

  test("PerPersonExemption looks right") {
    massert(
      trumpYears.forall(_.perPersonExemption == Deduction.zero)
    )
    massert(
      preTCJAYears.forall(_.perPersonExemption != Deduction.zero)
    )
  }

  test(
    "In an given year the set of bracket rates is the same" +
      "for all filing statuses"
  ) {
    allYears.foreach { year =>
      assertEquals(
        year.ordinaryBrackets.values.map(_.rates).toList.distinct.size,
        1,
        s"Year is $year"
      )
    }
  }

  test(
    "Pre trump means 2016-2017"
  ) {
    assertEquals(
      preTCJAYears.map(_.year.getValue).toSet,
      Set(2016, 2017)
    )
  }

  test("Every year Single pays more than HeadOfHousehold") {
    allYears.foreach { year =>
      massert(
        year.unadjustedStandardDeduction(Single) <=
          year.unadjustedStandardDeduction(HeadOfHousehold)
      )

      massert(
        year.unadjustedStandardDeduction(HeadOfHousehold) <=
          year.unadjustedStandardDeduction(Married)
      )

      // Note: we'd like to assert that Single thresholds all start lower
      // than the corresponding threshold, but not true, at least in 2019.
      // So we settle for this weaker metric.

      val sumOfSingleThresholds =
        year.ordinaryBrackets(Single).thresholds.toList.combineAll
      val sumOfHohThresholds =
        year.ordinaryBrackets(HeadOfHousehold).thresholds.toList.combineAll
      val sumOfMarriedThresholds =
        year.ordinaryBrackets(Married).thresholds.toList.combineAll

      massert(
        summon[Ordering[Income]]
          .lt(sumOfSingleThresholds, sumOfHohThresholds)
      )
      massert(
        summon[Ordering[Income]]
          .lt(sumOfHohThresholds, sumOfMarriedThresholds)
      )
    }
  }

  test("ordinaryBrackets are monotonically non-decreasing year over year") {
    for
      fs    <- FilingStatus.values
      years <- List(preTCJAYears, trumpYears)
    do
      val list = years.map(_.ordinaryBrackets(fs))
      list.zip(list.tail).foreach { (l, r) =>
        massert(l <= r, s"${fs.show}\n${l.show}\n${r.show}")
      }
  }

  test("qualifiedBrackets are monotonically non-decreasing year over year") {
    for
      fs    <- FilingStatus.values
      years <- List(preTCJAYears, trumpYears)
    do
      val list = years.map(_.qualifiedBrackets(fs))
      list.zip(list.tail).foreach { (l, r) =>
        massert(l <= r, s"${fs.show}\n${l.show}\n${r.show}")
      }
  }

  test("unadjustedStandardDeduction is monotonic year over year") {
    for
      fs    <- FilingStatus.values
      years <- List(preTCJAYears, trumpYears)
    do
      val list = years.map(_.unadjustedStandardDeduction(fs))
      list.zip(list.tail).foreach { (l, r) => massert(l <= r) }
  }

  test("perPersonExemption is monotonic year over year") {
    for years <- List(preTCJAYears, trumpYears)
    do
      val list = years.map(_.perPersonExemption)
      list.zip(list.tail).foreach { (l, r) => massert(l <= r) }
  }

  test("adjustmentWhenOver65 is monotonic year over year") {
    for years <- List(preTCJAYears, trumpYears)
    do
      val list = years.map(_.adjustmentWhenOver65)
      list.zip(list.tail).foreach { (l, r) => massert(l <= r) }
  }

  test("adjustmentWhenOver65ndSingle is monotonic year over year") {
    for years <- List(preTCJAYears, trumpYears)
    do
      val list = years.map(_.adjustmentWhenOver65AndSingle)
      list.zip(list.tail).foreach { (l, r) => massert(l <= r) }
  }

  test("expected congruencies hold") {
    for
      left  <- preTCJAYears
      right <- preTCJAYears
    do massert(left.hasCongruentOrdinaryBrackets(right))

    for
      left  <- trumpYears
      right <- trumpYears
    do massert(left.hasCongruentOrdinaryBrackets(right))

    for
      left  <- trumpYears ++ preTCJAYears
      right <- trumpYears ++ preTCJAYears
    do massert(left.hasCongruentQualifiedBrackets(right))
  }

  test("averageThresholdChangeOverPrevious is reasonable when present") {
    allYears.tail.foreach { values =>
      massert(YearlyValues.averageThresholdChangeOverPrevious(values.year).get > 1.0)
      massert(YearlyValues.averageThresholdChangeOverPrevious(values.year).get < 1.09)
    }
  }
  test("averageThresholdChangeOverPrevious matches results from spreadsheet") {
    // https://docs.google.com/spreadsheets/d/1Y_-LOViktEYW5hT-lY7XsU6vsPmCyg7s5sNkPxTKykI/edit#gid=0
    List(
      2018 -> 1.75,
      2019 -> 2.02,
      2020 -> 1.63,
      2021 -> 0.95,
      2022 -> 3.13,
      2023 -> 7.08
    ).foreach { (year, expectedPercentage) =>
      val change = YearlyValues.averageThresholdChangeOverPrevious(
        Year.of(year)
      ).get
      val percentage = ((change - 1.0) * 10000).round / 100.0
      massert(percentage == expectedPercentage)
    }
  }
end YearlyValuesSpec
