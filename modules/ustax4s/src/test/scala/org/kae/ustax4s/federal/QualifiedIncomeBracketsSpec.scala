package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.QualifiedIncomeBrackets
import org.kae.ustax4s.money.{Income, MoneyGeneration, TaxPayable, TaxableIncome}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

class QualifiedIncomeBracketsSpec
    extends ScalaCheckSuite
    with QualifiedBracketsGeneration
    with MoneyGeneration:

  // import math.Ordered.orderingToOrdered
  import TaxFunctions.*
  import math.Ordering.Implicits.infixOrderingOps

  private given Arbitrary[QualifiedIncomeBrackets] = Arbitrary(
    genQualifiedBrackets
  )
  private given Arbitrary[TaxableIncome] = Arbitrary(genTaxableIncome)

  test("InvestmentIncomeTaxBrackets should be progressive") {
    def isProgressive(brackets: QualifiedIncomeBrackets): Boolean = {
      val rates = brackets.bracketsAscending.map(_._2)
      (rates zip rates.tail)
        .forall { pair =>
          val (left, right) = pair
          left < right
        }
    }

    assert(isProgressive(QualifiedIncomeBrackets.of(Year.of(2021), Single)))
    assert(isProgressive(QualifiedIncomeBrackets.of(Year.of(2021), HeadOfHousehold)))
  }

  property("never tax zero gains") {
    forAll { (ordIncome: TaxableIncome, brackets: QualifiedIncomeBrackets) =>
      taxDueOnQualifiedIncome(brackets)(ordIncome, TaxableIncome.zero).isZero
    }
  }

  property("never tax gains in the lowest (zero-rate) bracket") {
    forAll { (brackets: QualifiedIncomeBrackets) =>
      val qualifiedIncome = brackets.startOfNonZeroQualifiedRateBracket
      taxDueOnQualifiedIncome(brackets)(TaxableIncome.zero, qualifiedIncome) == TaxPayable.zero
    }
  }

  property(
    "tax rises monotonically with qualified income outside the zero " +
      "bracket"
  ) {
    forAll { (brackets: QualifiedIncomeBrackets, gains1: TaxableIncome, gains2: TaxableIncome) =>
      {
        given Ordering[TaxableIncome] with
          def compare(x: TaxableIncome, y: TaxableIncome): Int =
            summon[Ordering[Income]].compare(x, y)
        val ordinaryIncome = brackets.startOfNonZeroQualifiedRateBracket
        val f              = taxDueOnQualifiedIncome(brackets)
        if gains1 < gains2 then f(ordinaryIncome, gains1) < f(ordinaryIncome, gains2)
        else if gains1 > gains2 then f(ordinaryIncome, gains1) > f(ordinaryIncome, gains2)
        else f(ordinaryIncome, gains1) == f(ordinaryIncome, gains2)
      }
    }
  }

  property("tax rises monotonically with ordinary income") {
    forAll {
      (
        brackets: QualifiedIncomeBrackets,
        gains: TaxableIncome,
        income1: TaxableIncome,
        income2: TaxableIncome
      ) =>
        given Ordering[TaxableIncome] with
          def compare(x: TaxableIncome, y: TaxableIncome): Int =
            summon[Ordering[Income]].compare(x, y)
        val f = taxDueOnQualifiedIncome(brackets)
        val res = {
          if income1 < income2 then f(income1, gains) <= f(income1, gains)
          else if income1 > income2 then f(income1, gains) >= f(income2, gains)
          else f(income1, gains) == f(income2, gains)
        }
        if !res then
          println(brackets.show)
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
    forAll { (brackets: QualifiedIncomeBrackets, gains: TaxableIncome) =>
      val ordinaryIncome = brackets.startOfNonZeroQualifiedRateBracket
      taxDueOnQualifiedIncome(brackets)(ordinaryIncome, gains).nonZero || gains.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (brackets: QualifiedIncomeBrackets, gains: TaxableIncome) =>
      val maxTax = gains taxAt brackets.bracketsAscending.map(_._2).max
      taxDueOnQualifiedIncome(brackets)(TaxableIncome.zero, gains) <= maxTax
    }
  }
