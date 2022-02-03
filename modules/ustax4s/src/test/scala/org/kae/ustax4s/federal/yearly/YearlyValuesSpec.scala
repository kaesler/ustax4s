package org.kae.ustax4s.federal
package yearly

import cats.implicits.*
import cats.syntax.*
import munit.{Assertions, FunSuite}
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

  private val preTrumpYears = allYears.filter(_.regime == PreTrump)
  private val trumpYears    = allYears.filter(_.regime == Trump)

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
      preTrumpYears.forall(_.perPersonExemption != Deduction.zero)
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
      preTrumpYears.map(_.year.getValue).toSet,
      Set(2016, 2017)
    )
  }

  test("Every year Single pays more than HeadOfHousehold") {
    allYears.foreach { year =>
      massert(
        year.unadjustedStandardDeduction(Single) <=
          year.unadjustedStandardDeduction(HeadOfHousehold)
      )

      // Note: we'd like to assert that Single thresholds all start lower
      // than the corresponding threshold, but not true, at least in 2019.
      // So we settle for this weaker metric.

      val sumOfSingleThresholds =
        year.ordinaryBrackets(Single).thresholds.toList.combineAll
      val sumOfHohThresholds =
        year.ordinaryBrackets(HeadOfHousehold).thresholds.toList.combineAll

      massert(
        summon[Ordering[IncomeThreshold]]
          .lt(sumOfSingleThresholds, sumOfHohThresholds)
      )
    }
  }

  test("ordinaryBrackets are monotonically non-decreasing year over year") {
    for
      fs    <- List(Single, HeadOfHousehold)
      years <- List(preTrumpYears, trumpYears)
    do
      val list = years.map(_.ordinaryBrackets(fs))
      list.zip(list.tail).foreach { (l, r) =>
        massert(l <= r, s"${fs.show}\n${l.show}\n${r.show}")
      }
  }

  test("qualifiedBrackets are monotonically non-decreasing year over year") {
    for
      fs    <- List(Single, HeadOfHousehold)
      years <- List(preTrumpYears, trumpYears)
    do
      val list = years.map(_.qualifiedBrackets(fs))
      list.zip(list.tail).foreach { (l, r) =>
        massert(l <= r, s"${fs.show}\n${l.show}\n${r.show}")
      }
  }

  test("unadjustedStandardDeduction is monotonic year over year") {
    for
      fs    <- List(Single, HeadOfHousehold)
      years <- List(preTrumpYears, trumpYears)
    do
      val list = years.map(_.unadjustedStandardDeduction(fs))
      list.zip(list.tail).foreach { (l, r) => massert(l <= r) }
  }

  test("perPersonExemption is monotonic year over year") {
    for years <- List(preTrumpYears, trumpYears)
    do
      val list = years.map(_.perPersonExemption)
      list.zip(list.tail).foreach { (l, r) => massert(l <= r) }
  }

  test("adjustmentWhenOver65 is monotonic year over year") {
    for years <- List(preTrumpYears, trumpYears)
    do
      val list = years.map(_.adjustmentWhenOver65)
      list.zip(list.tail).foreach { (l, r) => massert(l <= r) }
  }

  test("adjustmentWhenOver65ndSingle is monotonic year over year") {
    for years <- List(preTrumpYears, trumpYears)
    do
      val list = years.map(_.adjustmentWhenOver65AndSingle)
      list.zip(list.tail).foreach { (l, r) => massert(l <= r) }
  }

end YearlyValuesSpec