package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.{LocalDate, Year}
import munit.{FunSuite, ScalaCheckSuite}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.*
import org.kae.ustax4s.{FilingStatus, InflationEstimate}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen}

class BoundRegimeFutureEstimationSpec extends ScalaCheckSuite:
  import BoundRegimeFutureEstimationSpec.*
  import math.Ordering.Implicits.infixOrderingOps
  import org.kae.ustax4s.money.MoneyConversions.given

  private val birthDate: LocalDate = LocalDate.of(1955, 10, 2)

  property("BoundRegime.forFutureYear affects tax due monotonically decreasing") {
    forAll { (tc: TestCase2) =>

      val before = BoundRegime.forKnownYear(
        YearlyValues.lastFor(tc.regime).year,
        birthDate,
        tc.filingStatus,
        tc.personalExemptions
      ).calculator

      val after = BoundRegime.forFutureYear(
        tc.regime,
        tc.futureYear,
        tc.inflationFactorEstimate,
        birthDate,
        tc.filingStatus,
        tc.personalExemptions
      ).calculator

      val taxResultsBefore = before.federalTaxResults(
        tc.ss,
        tc.ordinaryIncomeNonSS,
        tc.qualifiedIncome,
        tc.itemizedDeductions
      )
      val taxResultsAfter = after.federalTaxResults(
        tc.ss,
        tc.ordinaryIncomeNonSS,
        tc.qualifiedIncome,
        tc.itemizedDeductions
      )
      val res = taxResultsBefore.taxDue >= taxResultsAfter.taxDue
      if !res then {
        println(taxResultsBefore.show)
        println(taxResultsAfter.show)
      }
      res
    }
  }

  property("BoundRegime.forFutureYear affects BoundRegime fields monotonically") {
    forAll { (tc: TestCase2) =>

      val before = BoundRegime.forKnownYear(
        YearlyValues.lastFor(tc.regime).year,
        birthDate,
        tc.filingStatus,
        tc.personalExemptions
      )

      val after = BoundRegime.forFutureYear(
        tc.regime,
        tc.futureYear,
        tc.inflationFactorEstimate,
        birthDate,
        tc.filingStatus,
        tc.personalExemptions
      )

      val res =
        (before.standardDeduction <= after.standardDeduction) &&
          (before.netDeduction(0) <= after.netDeduction(0)) &&
          (before.ordinaryBrackets <= after.ordinaryBrackets) &&
          (before.qualifiedBrackets <= after.qualifiedBrackets)
      if !res then {
        println(before.show)
        println(after.show)
      }
      res
    }
  }

  property("BoundRegime.withEstimatedNetInflationFactor affects tax due monotonically") {
    forAll { (tc: TestCase1) =>
      val before = BoundRegime.forKnownYear(
        tc.baseYear,
        birthDate,
        tc.filingStatus,
        tc.personalExemptions
      )

      val after = before.withEstimatedNetInflationFactor(
        tc.futureYear,
        tc.inflationFactorEstimate
      )
      val taxResultsBefore = before.calculator.federalTaxResults(
        tc.ss,
        tc.ordinaryIncomeNonSS,
        tc.qualifiedIncome,
        tc.itemizedDeductions
      )
      val taxResultsAfter = after.calculator.federalTaxResults(
        tc.ss,
        tc.ordinaryIncomeNonSS,
        tc.qualifiedIncome,
        tc.itemizedDeductions
      )
      val res = taxResultsBefore.taxDue >= taxResultsAfter.taxDue
      if !res then {
        println(taxResultsBefore.show)
        println(taxResultsAfter.show)
      }
      res
    }
  }

  property("BoundRegime.withEstimatedNetInflationFactor affects fields monotonically") {
    forAll { (tc: TestCase1) =>
      val before = BoundRegime.forKnownYear(
        tc.baseYear,
        birthDate,
        tc.filingStatus,
        tc.personalExemptions
      )

      val after = before.withEstimatedNetInflationFactor(
        tc.futureYear,
        tc.inflationFactorEstimate
      )
      val res =
        (before.standardDeduction <= after.standardDeduction) &&
          (before.netDeduction(0) <= after.netDeduction(0)) &&
          (before.ordinaryBrackets <= after.ordinaryBrackets) &&
          (before.qualifiedBrackets <= after.qualifiedBrackets)
      if !res then {
        println(before.show)
        println(after.show)
      }
      res
    }
  }

end BoundRegimeFutureEstimationSpec

object BoundRegimeFutureEstimationSpec:
  import org.kae.ustax4s.money.MoneyConversions.given

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
    for {
      baseYear                <- Gen.choose(2017, 2022).map(Year.of)
      futureYear              <- Gen.choose(2023, 2055).map(Year.of)
      inflationFactorEstimate <- Gen.choose(1.005, 1.10)
      filingStatus            <- Gen.oneOf(FilingStatus.values.toList)
      personalExemptions      <- Gen.choose(0, 4)
      ss                      <- Gen.chooseNum(0, 50000)
      ordinaryIncomeNonSS     <- Gen.chooseNum(0, 150000)
      qualifiedIncome         <- Gen.chooseNum(0, 100000)
      itemizedDeductions      <- Gen.chooseNum(0, 30000)
    } yield TestCase1(
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
    regime: Regime,
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
    for {
      regime                  <- Gen.oneOf(Regime.values.toList)
      futureYear              <- Gen.choose(2023, 2055).map(Year.of)
      inflationFactorEstimate <- Gen.choose(1.005, 1.10)
      filingStatus            <- Gen.oneOf(FilingStatus.values.toList)
      personalExemptions      <- Gen.choose(0, 4)
      ss                      <- Gen.chooseNum(0, 50000)
      ordinaryIncomeNonSS     <- Gen.chooseNum(0, 150000)
      qualifiedIncome         <- Gen.chooseNum(0, 100000)
      itemizedDeductions      <- Gen.chooseNum(0, 30000)
    } yield TestCase2(
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

end BoundRegimeFutureEstimationSpec
