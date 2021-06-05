package org.kae.ustax4s

import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

class QualifiedIncomeBracketsSpec
    extends ScalaCheckSuite
    with QualifiedBracketsGeneration
    with TMoneyGeneration:

  private implicit val arbQualifiedBrackets: Arbitrary[QualifiedIncomeBrackets] = Arbitrary(
    genQualifiedBrackets
  )
  private implicit val arbIncome: Arbitrary[TMoney] = Arbitrary(genMoney)

  private val zero = TMoney.zero

  test("InvestmentIncomeTaxBrackets should be progressive") {
    def isProgressive(brackets: QualifiedIncomeBrackets): Boolean = {
      val rates = brackets.bracketStartsAscending.map(_._2)
      (rates zip rates.tail)
        .forall { case (left, right) =>
          left < right
        }
    }

    assert(isProgressive(QualifiedIncomeBrackets.of(Year.of(2021), Single)))
    assert(isProgressive(QualifiedIncomeBrackets.of(Year.of(2021), HeadOfHousehold)))
  }

  property("never tax zero gains") {
    forAll { (ordIncome: TMoney, brackets: QualifiedIncomeBrackets) =>
      brackets.taxDueFunctionally(ordIncome, zero) == zero
    }
  }

  property("never tax gains in the lowest (zero-rate) bracket") {
    forAll { (brackets: QualifiedIncomeBrackets) =>
      val qualifiedIncome = brackets.startOfNonZeroQualifiedRateBracket
      brackets.taxDueFunctionally(zero, qualifiedIncome) == zero
    }
  }

  property(
    "tax rises monotonically with qualified income outside the zero " +
      "bracket"
  ) {
    forAll { (brackets: QualifiedIncomeBrackets, gains1: TMoney, gains2: TMoney) =>
      {
        val ordinaryIncome = brackets.startOfNonZeroQualifiedRateBracket
        if gains1 < gains2 then
          brackets.taxDueFunctionally(ordinaryIncome, gains1) < brackets
            .taxDueFunctionally(
              ordinaryIncome,
              gains2
            )
        else if gains1 > gains2 then
          brackets.taxDueFunctionally(ordinaryIncome, gains1) > brackets
            .taxDueFunctionally(
              ordinaryIncome,
              gains2
            )
        else
          brackets.taxDueFunctionally(ordinaryIncome, gains1) == brackets
            .taxDueFunctionally(
              ordinaryIncome,
              gains2
            )
      }
    }
  }

  property("tax rises monotonically with ordinary income") {
    forAll { (brackets: QualifiedIncomeBrackets, gains: TMoney, income1: TMoney, income2: TMoney) =>
      val res = {
        if (income1 < income2)
          brackets
            .taxDueFunctionally(income1, gains) <= brackets
            .taxDueFunctionally(income1, gains)
        else if income1 > income2 then
          brackets
            .taxDueFunctionally(income1, gains) >= brackets
            .taxDueFunctionally(income2, gains)
        else
          brackets.taxDueFunctionally(income1, gains) == brackets
            .taxDueFunctionally(
              income2,
              gains
            )
      }
      if (!res) {
        println(brackets.show)
        println(s"gains: $gains")
        println(s"income1: $income1; tax: ${brackets.taxDueFunctionally(income1, gains)}")
        println(s"income2: $income2; tax: ${brackets.taxDueFunctionally(income2, gains)}")
      }
      res
    }
  }

  property(
    "tax is never zero except on zero gains, outside the bottom " +
      "rate"
  ) {
    forAll { (brackets: QualifiedIncomeBrackets, gains: TMoney) =>
      val ordinaryIncome = brackets.startOfNonZeroQualifiedRateBracket
      brackets.taxDueFunctionally(ordinaryIncome, gains).nonZero || gains.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (brackets: QualifiedIncomeBrackets, gains: TMoney) =>
      val maxTax = gains * brackets.bracketStartsAscending.map(_._2).max
      brackets.taxDueFunctionally(zero, gains) <= maxTax
    }
  }
