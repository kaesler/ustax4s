package org.kae.ustax4s.federal

import cats.syntax.all.*
import java.time.{LocalDate, Year}
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus.{MarriedJoint, Single}
import org.kae.ustax4s.{Age, FilingStatus}
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.money.Moneys.{Deduction, Income, TaxableIncome}
import org.scalacheck.Test.Parameters
import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.{Arbitrary, Gen}
import scala.math.Ordered.orderingToOrdered

class MeansTestedSeniorDeductionSpec extends ScalaCheckSuite:
  import MeansTestedSeniorDeductionSpec.{*, given}

  implicit val params: Parameters = Parameters.default
    .withMinSuccessfulTests(5000)

  val yearsWhenItIsInForce: Gen[Year] = Gen.oneOf(2025.to(2028).map(Year.of))

  property("meansTestedSeniorDeduction is only non-zero 2025-28") {
    forAll { (sc: Scenario) =>
      import sc.*
      val res = TaxCalculator.federalTaxResultsForAnyYear(
        1.03,
        year,
        filingStatus,
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      //  zero unless
      //    in the years in effect
      //    over 65
      res.meansTestedSeniorDeduction == Deduction.zero ||
      (Set(2025, 2026, 2027, 2028).contains(sc.year.getValue) &&
        Age.isAge65OrOlder(birthDate, year))
    }
  }

  property("meansTestedSeniorDeduction allowed in full for singles") {
    val birthDateOver65 = LocalDate.of(1955, 10, 2)
    forAll { (scenario: Scenario) =>
      forAll(yearsWhenItIsInForce) { yearInForce =>
        val sc = scenario.copy(
          year = yearInForce,
          birthDate = birthDateOver65,
          filingStatus = Single
        )
        import sc.*
        val res = TaxCalculator.federalTaxResultsForAnyYear(
          1.03,
          year,
          filingStatus,
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )

        val success = res.agi > Income(75000) ||
          res.meansTestedSeniorDeduction == Deduction(6000)
        if !success then println(res.show)
        success
      }
    }
  }

  property("meansTestedSeniorDeduction allowed in full for marrieds") {
    val birthDateOver65 = LocalDate.of(1955, 10, 2)
    forAll { (scenario: Scenario) =>
      forAll(yearsWhenItIsInForce) { yearInForce =>
        val sc = scenario.copy(
          year = yearInForce,
          birthDate = birthDateOver65,
          filingStatus = MarriedJoint
        )
        import sc.*
        val res = TaxCalculator.federalTaxResultsForAnyYear(
          1.03,
          sc.year,
          filingStatus,
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )

        val success = res.agi > Income(150000) ||
          res.meansTestedSeniorDeduction == Deduction(12000)
        if !success then println(res.show)
        success
      }
    }
  }

  property("meansTestedSeniorDeduction phases out for singles") {
    val birthDateOver65 = LocalDate.of(1955, 10, 2)
    forAll { (scenario: Scenario) =>
      forAll(yearsWhenItIsInForce) { yearInForce =>
        val sc = scenario.copy(
          year = yearInForce,
          birthDate = birthDateOver65,
          filingStatus = Single
        )
        import sc.*
        val res = TaxCalculator.federalTaxResultsForAnyYear(
          1.03,
          year,
          filingStatus,
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )

        val success = res.agi < Income(175000) ||
          res.meansTestedSeniorDeduction == Deduction(0)
        if !success then println(res.show)
        success
      }
    }
  }

  property("meansTestedSeniorDeduction phases out for marrieds") {
    val birthDateOver65 = LocalDate.of(1955, 10, 2)
    forAll { (scenario: Scenario) =>
      forAll(yearsWhenItIsInForce) { yearInForce =>
        val sc = scenario.copy(
          year = yearInForce,
          birthDate = birthDateOver65,
          filingStatus = MarriedJoint
        )
        import sc.*
        val res = TaxCalculator.federalTaxResultsForAnyYear(
          1.03,
          year,
          filingStatus,
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )

        val success = res.agi < Income(275000) ||
          res.meansTestedSeniorDeduction == Deduction(0)
        if !success then println(res.show)
        success
      }
    }
  }

  property("meansTestedSeniorDeduction allowed in part for singles") {
    val birthDateOver65 = LocalDate.of(1955, 10, 2)
    forAll { (scenario: Scenario) =>
      forAll(yearsWhenItIsInForce) { yearInForce =>
        val sc = scenario.copy(
          year = yearInForce,
          birthDate = birthDateOver65,
          filingStatus = Single
        )
        import sc.*
        val res = TaxCalculator.federalTaxResultsForAnyYear(
          1.03,
          year,
          filingStatus,
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )

        // println(res.show)
        val ded = res.meansTestedSeniorDeduction
        (ded > Deduction.zero && ded < Deduction(6000)) ||
        res.agi < Income(75000) || res.agi > Income(175000)
      }
    }
  }

end MeansTestedSeniorDeductionSpec

object MeansTestedSeniorDeductionSpec:

  final case class Scenario(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    // Self plus dependents
    personalExemptions: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  )

  private given Arbitrary[Scenario] =
    Arbitrary(
      for
        year         <- Gen.choose(2025, 2035).map(Year.of)
        filingStatus <- Gen.oneOf(FilingStatus.values.toList)
        birthDate    <- Gen.oneOf(
          LocalDate.of(1955, 10, 2),
          LocalDate.now().minusYears(50)
        )
        socSec              <- Gen.chooseNum(0, 60000).map(Income.apply)
        ordinaryIncomeNonSS <- Gen.chooseNum(0, 150000).map(Income.apply)
        qualifiedIncome     <- Gen.chooseNum(0, 100000).map(TaxableIncome.apply)
        itemizedDeductions  <- Gen.chooseNum(0, 30000).map(Deduction.apply)
      yield Scenario(
        year,
        filingStatus,
        birthDate,
        personalExemptions = 0,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
    )
  end given
end MeansTestedSeniorDeductionSpec
