package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.{Income, MoneyGeneration, TaxPayable, TaxableIncome}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

class QualifiedRateFunctionSpec
    extends ScalaCheckSuite
    with QualifiedBracketsGeneration
    with MoneyGeneration:

  import FedTaxFunctions.*
  import math.Ordering.Implicits.infixOrderingOps

  private given Arbitrary[QualifiedRateFunction] = Arbitrary(
    genQualifiedRateFunction
  )
  private given Arbitrary[TaxableIncome] = Arbitrary(genTaxableIncome)

  test("QualifiedTaxBrackets should be progressive") {
    def isProgressive(qrf: QualifiedRateFunction): Boolean = {
      val rates = qrf.bracketsAscending.map(_.rate)
      (rates zip rates.tail)
        .forall { pair =>
          val (left, right) = pair
          left < right
        }
    }

    assert(
      isProgressive(
        YearlyValues.of(Year.of(2021)).get.qualifiedRateFunctions(Single)
      )
    )
    assert(
      isProgressive(
        YearlyValues.of(Year.of(2021)).get.qualifiedRateFunctions(HeadOfHousehold)
      )
    )
  }

  property("never tax zero gains") {
    forAll { (ordIncome: TaxableIncome, qrf: QualifiedRateFunction) =>
      taxPayableOnQualifiedIncome(qrf)(ordIncome, TaxableIncome.zero).isZero
    }
  }

  property("never tax gains in the lowest (zero-rate) bracket") {
    forAll { (qrf: QualifiedRateFunction) =>
      val qualifiedIncome = qrf.startOfNonZeroQualifiedRateBracket
      taxPayableOnQualifiedIncome(qrf)(TaxableIncome.zero, qualifiedIncome) == TaxPayable.zero
    }
  }

  property(
    "tax rises monotonically with qualified income outside the zero " +
      "bracket"
  ) {
    forAll { (qrf: QualifiedRateFunction, gains1: TaxableIncome, gains2: TaxableIncome) =>
      {
        given Ordering[TaxableIncome]:
          def compare(x: TaxableIncome, y: TaxableIncome): Int =
            summon[Ordering[Income]].compare(x, y)
        val ordinaryIncome = qrf.startOfNonZeroQualifiedRateBracket
        val f              = taxPayableOnQualifiedIncome(qrf)
        if gains1 < gains2 then f(ordinaryIncome, gains1) < f(ordinaryIncome, gains2)
        else if gains1 > gains2 then f(ordinaryIncome, gains1) > f(ordinaryIncome, gains2)
        else f(ordinaryIncome, gains1) == f(ordinaryIncome, gains2)
      }
    }
  }

  property("tax rises monotonically with ordinary income") {
    forAll {
      (
        qrf: QualifiedRateFunction,
        gains: TaxableIncome,
        income1: TaxableIncome,
        income2: TaxableIncome
      ) =>
        given Ordering[TaxableIncome]:
          def compare(x: TaxableIncome, y: TaxableIncome): Int =
            summon[Ordering[Income]].compare(x, y)
            
        val f = taxPayableOnQualifiedIncome(qrf)
        val res = {
          if income1 < income2 then f(income1, gains) <= f(income1, gains)
          else if income1 > income2 then f(income1, gains) >= f(income2, gains)
          else f(income1, gains) == f(income2, gains)
        }
        if !res then
          println(qrf.show)
          println(s"gains: $gains")
          println(s"income1: $income1; tax: ${f(income1, gains)}")
          println(s"income2: $income2; tax: ${f(income2, gains)}")
        res
    }
  }

  property(
    "tax is never zero except on zero gains, outside the bottom " +
      "rate"
  ) {
    forAll { (qrf: QualifiedRateFunction, gains: TaxableIncome) =>
      val ordinaryIncome = qrf.startOfNonZeroQualifiedRateBracket
      taxPayableOnQualifiedIncome(qrf)(ordinaryIncome, gains).nonZero || gains.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (qrf: QualifiedRateFunction, gains: TaxableIncome) =>
      val maxTax = gains taxAt qrf.bracketsAscending.map(_.rate).max
      taxPayableOnQualifiedIncome(qrf)(TaxableIncome.zero, gains) <= maxTax
    }
  }
