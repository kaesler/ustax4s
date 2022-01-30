package org.kae.ustax4s.federal
package yearly

import cats.implicits.*
import cats.PartialOrder
import org.kae.ustax4s.money.Deduction
import munit.{Assertions, FunSuite}
import org.kae.ustax4s.money.IncomeThreshold

// Tests that the static data encoding yearly values is correctly entered.
// TODO some sanity tests for this static data:
//   - consistent bracket rates
//   - correct regime for year
//   - monotonicity by year
//     - deduction values
//     - ordinary bracket thresholds
//     - qualified bracket thresholds
//   - monotonicity by FS within each year: Single < HeadOfHousehold
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

  private def massert(b: Boolean): Unit = Assertions.assert(b)

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
        1
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

end YearlyValuesSpec
