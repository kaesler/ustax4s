package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.QualifiedIncomeBrackets
import org.kae.ustax4s.money.{Money, MoneyGeneration}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

class QualifiedIncomeBracketsSpec
    extends ScalaCheckSuite
    with QualifiedBracketsGeneration
    with MoneyGeneration:

  private given Arbitrary[QualifiedIncomeBrackets] = Arbitrary(
    genQualifiedBrackets
  )
  private given Arbitrary[Money] = Arbitrary(genMoney)

  private val zero = Money(0)

  test("InvestmentIncomeTaxBrackets should be progressive") {
    def isProgressive(brackets: QualifiedIncomeBrackets): Boolean = {
      val rates = brackets.bracketStartsAscending.map(_._2)
      (rates zip rates.tail)
        .forall { (left, right) =>
          left < right
        }
    }

    assert(isProgressive(QualifiedIncomeBrackets.of(Year.of(2021), Single)))
    assert(isProgressive(QualifiedIncomeBrackets.of(Year.of(2021), HeadOfHousehold)))
  }

  property("never tax zero gains") {
    forAll { (ordIncome: Money, brackets: QualifiedIncomeBrackets) =>
      brackets.taxDue(ordIncome, zero) == zero
    }
  }

  property("never tax gains in the lowest (zero-rate) bracket") {
    forAll { (brackets: QualifiedIncomeBrackets) =>
      val qualifiedIncome = brackets.startOfNonZeroQualifiedRateBracket
      brackets.taxDue(zero, qualifiedIncome) == zero
    }
  }

  property(
    "tax rises monotonically with qualified income outside the zero " +
      "bracket"
  ) {
    forAll { (brackets: QualifiedIncomeBrackets, gains1: Money, gains2: Money) =>
      {
        val ordinaryIncome = brackets.startOfNonZeroQualifiedRateBracket
        if gains1 < gains2 then
          brackets.taxDue(ordinaryIncome, gains1) < brackets
            .taxDue(
              ordinaryIncome,
              gains2
            )
        else if gains1 > gains2 then
          brackets.taxDue(ordinaryIncome, gains1) > brackets
            .taxDue(
              ordinaryIncome,
              gains2
            )
        else
          brackets.taxDue(ordinaryIncome, gains1) == brackets
            .taxDue(
              ordinaryIncome,
              gains2
            )
      }
    }
  }

  property("tax rises monotonically with ordinary income") {
    forAll { (brackets: QualifiedIncomeBrackets, gains: Money, income1: Money, income2: Money) =>
      val res = {
        if income1 < income2 then
          brackets
            .taxDue(income1, gains) <= brackets
            .taxDue(income1, gains)
        else if income1 > income2 then
          brackets
            .taxDue(income1, gains) >= brackets
            .taxDue(income2, gains)
        else
          brackets.taxDue(income1, gains) == brackets
            .taxDue(
              income2,
              gains
            )
      }
      if !res then
        println(brackets.show)
        println(s"gains: $gains")
        println(s"income1: $income1; tax: ${brackets.taxDue(income1, gains)}")
        println(s"income2: $income2; tax: ${brackets.taxDue(income2, gains)}")
      res
    }
  }

  property(
    "tax is never zero except on zero gains, outside the bottom " +
      "rate"
  ) {
    forAll { (brackets: QualifiedIncomeBrackets, gains: Money) =>
      val ordinaryIncome = brackets.startOfNonZeroQualifiedRateBracket
      brackets.taxDue(ordinaryIncome, gains).nonZero || gains.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (brackets: QualifiedIncomeBrackets, gains: Money) =>
      val maxTax = gains taxAt brackets.bracketStartsAscending.map(_._2).max
      brackets.taxDue(zero, gains) <= maxTax
    }
  }
