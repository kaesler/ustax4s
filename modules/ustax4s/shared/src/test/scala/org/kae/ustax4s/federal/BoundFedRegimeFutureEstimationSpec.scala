package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.{LocalDate, Year}
import munit.{FunSuite, ScalaCheckSuite}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.*
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen}
import scala.language.implicitConversions

class BoundFedRegimeFutureEstimationSpec extends ScalaCheckSuite:
  import BoundFedRegimeFutureEstimationSpec.*
  import math.Ordering.Implicits.infixOrderingOps
  import org.kae.ustax4s.money.MoneyConversions.given

  private val birthDateUnder65: LocalDate = LocalDate.now().minusYears(50)

  // Note: the 2025 Trump senior tax deduction is temporary (2026-8) so it breaks monotonicity.

  property("BoundRegime.forFutureYear affects tax due monotonically decreasing for under 65") {
    val birthDate = birthDateUnder65
    forAll { (tc: TestCase2) =>

      val before = BoundFedRegime
        .forKnownYear(
          YearlyValues.mostRecentFor(tc.regime).year,
          tc.filingStatus
        )
        .calculator

      val after = BoundFedRegime
        .forFutureYear(
          tc.regime,
          tc.futureYear,
          tc.inflationFactorEstimate,
          tc.filingStatus
        )
        .calculator

      val taxResultsBefore = before.apply(
        CalcInput(
          birthDate,
          tc.personalExemptions,
          tc.ss,
          tc.ordinaryIncomeNonSS,
          tc.qualifiedIncome,
          tc.itemizedDeductions
        )
      )
      val taxResultsAfter = after.apply(
        CalcInput(
          birthDate,
          tc.personalExemptions,
          tc.ss,
          tc.ordinaryIncomeNonSS,
          tc.qualifiedIncome,
          tc.itemizedDeductions
        )
      )
      val res = taxResultsBefore.taxPayable >= taxResultsAfter.taxPayable
      if !res then {
        println(taxResultsBefore.show)
        println(taxResultsAfter.show)
      }
      res
    }
  }

  property("BoundRegime.forFutureYear affects BoundRegime fields monotonically for under 65") {
    val birthDate = birthDateUnder65
    forAll { (tc: TestCase2) =>

      val before = BoundFedRegime.forKnownYear(
        YearlyValues.mostRecentFor(tc.regime).year,
        tc.filingStatus
      )

      val after = BoundFedRegime.forFutureYear(
        tc.regime,
        tc.futureYear,
        tc.inflationFactorEstimate,
        tc.filingStatus
      )

      val closeEnoughToAgi = tc.ordinaryIncomeNonSS + tc.qualifiedIncome
      val res              =
        before.standardDeduction(birthDate) <= after.standardDeduction(birthDate) &&
          before.netDeduction(closeEnoughToAgi, birthDate, tc.personalExemptions, 0) <=
          after.netDeduction(closeEnoughToAgi, birthDate, tc.personalExemptions, 0) &&
          before.ordinaryRateFunction <= after.ordinaryRateFunction &&
          before.qualifiedRateFunction <= after.qualifiedRateFunction
      if !res then {
        println(before.show)
        println(after.show)
      }
      res
    }
  }

  property(
    "BoundRegime.withEstimatedNetInflationFactor affects tax due monotonically for under 65"
  ) {
    val birthDate = birthDateUnder65
    forAll { (tc: TestCase1) =>
      val before = BoundFedRegime.forKnownYear(
        tc.baseYear,
        tc.filingStatus
      )

      val after = before.withEstimatedNetInflationFactor(
        tc.futureYear,
        tc.inflationFactorEstimate
      )
      val taxResultsBefore = before.calculator.apply(
        CalcInput(
          birthDate,
          tc.personalExemptions,
          tc.ss,
          tc.ordinaryIncomeNonSS,
          tc.qualifiedIncome,
          tc.itemizedDeductions
        )
      )
      val taxResultsAfter = after.calculator.apply(
        CalcInput(
          birthDate,
          tc.personalExemptions,
          tc.ss,
          tc.ordinaryIncomeNonSS,
          tc.qualifiedIncome,
          tc.itemizedDeductions
        )
      )
      val res = taxResultsBefore.taxPayable >= taxResultsAfter.taxPayable
      if !res then {
        println(taxResultsBefore.show)
        println(taxResultsAfter.show)
      }
      res
    }
  }

  property(
    "BoundRegime.withEstimatedNetInflationFactor affects fields monotonically for under 65"
  ) {
    val birthDate = birthDateUnder65
    forAll { (tc: TestCase1) =>
      val before = BoundFedRegime.forKnownYear(
        tc.baseYear,
        tc.filingStatus
      )

      val after = before.withEstimatedNetInflationFactor(
        tc.futureYear,
        tc.inflationFactorEstimate
      )
      val closeEnoughToAgi = tc.ordinaryIncomeNonSS + tc.qualifiedIncome
      val res              =
        (before.standardDeduction(birthDate) <= after.standardDeduction(birthDate)) &&
          (before.netDeduction(closeEnoughToAgi, birthDate, tc.personalExemptions, 0) <=
            after.netDeduction(closeEnoughToAgi, birthDate, tc.personalExemptions, 0)) &&
          (before.ordinaryRateFunction <= after.ordinaryRateFunction) &&
          (before.qualifiedRateFunction <= after.qualifiedRateFunction)
      if !res then {
        println(before.show)
        println(after.show)
      }
      res
    }
  }

end BoundFedRegimeFutureEstimationSpec

object BoundFedRegimeFutureEstimationSpec:
  import org.kae.ustax4s.money.MoneyConversions.given

  private val lastKnownYear   = YearlyValues.last.year.getValue
  private val firstFutureYear = lastKnownYear + 1

  private final case class TestCase1(
    baseYear: Year,
    futureYear: Year,
    inflationFactorEstimate: Double,
    filingStatus: FilingStatus,
    personalExemptions: Int,
    ss: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  )

  private given Arbitrary[TestCase1] = Arbitrary(
    for
      baseYear                <- Gen.choose(2017, lastKnownYear).map(Year.of)
      futureYear              <- Gen.choose(firstFutureYear, 2055).map(Year.of)
      inflationFactorEstimate <- Gen.choose(1.005, 1.10)
      filingStatus            <- Gen.oneOf(FilingStatus.values.toList)
      personalExemptions      <- Gen.choose(0, 4)
      ss                      <- Gen.chooseNum(0, 50000)
      ordinaryIncomeNonSS     <- Gen.chooseNum(0, 150000)
      qualifiedIncome         <- Gen.chooseNum(0, 100000)
      itemizedDeductions      <- Gen.chooseNum(0, 30000)
    yield TestCase1(
      baseYear,
      futureYear,
      inflationFactorEstimate,
      filingStatus,
      personalExemptions,
      ss,
      ordinaryIncomeNonSS,
      qualifiedIncome,
      itemizedDeductions
    )
  )

  private final case class TestCase2(
    regime: FedRegime,
    futureYear: Year,
    inflationFactorEstimate: Double,
    filingStatus: FilingStatus,
    personalExemptions: Int,
    ss: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  )

  private given Arbitrary[TestCase2] = Arbitrary(
    for
      regime                  <- Gen.oneOf(FedRegime.values.toList)
      futureYear              <- Gen.choose(firstFutureYear, 2055).map(Year.of)
      inflationFactorEstimate <- Gen.choose(1.005, 1.10)
      filingStatus            <- Gen.oneOf(FilingStatus.values.toList)
      personalExemptions      <- Gen.choose(0, 4)
      ss                      <- Gen.chooseNum(0, 50000)
      ordinaryIncomeNonSS     <- Gen.chooseNum(0, 150000)
      qualifiedIncome         <- Gen.chooseNum(0, 100000)
      itemizedDeductions      <- Gen.chooseNum(0, 30000)
    yield TestCase2(
      regime,
      futureYear,
      inflationFactorEstimate,
      filingStatus,
      personalExemptions,
      ss,
      ordinaryIncomeNonSS,
      qualifiedIncome,
      itemizedDeductions
    )
  )

end BoundFedRegimeFutureEstimationSpec
