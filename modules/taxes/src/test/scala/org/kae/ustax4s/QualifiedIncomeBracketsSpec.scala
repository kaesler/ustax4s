package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.scalacheck.Arbitrary
import org.specs2.ScalaCheck
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object QualifiedIncomeBracketsSpec
    extends Specification
    with ScalaCheck
    with QualifiedBracketsGeneration
    with TMoneyGeneration
    with MustMatchers {

  private implicit val arbQualifiedBrackets: Arbitrary[QualifiedIncomeBrackets] = Arbitrary(
    genQualifiedBrackets
  )
  private implicit val arbIncome: Arbitrary[TMoney] = Arbitrary(genMoney)

  private val zero = TMoney.zero

  "InvestmentIncomeTaxBrackets should" >> {

    "be progressive" >> {
      def isProgressive(brackets: QualifiedIncomeBrackets): Boolean = {
        val rates = brackets.bracketStartsAscending.map(_._2)
        (rates zip rates.tail)
          .forall { case (left, right) =>
            left < right
          }
      }

      isProgressive(QualifiedIncomeBrackets.of(Year.of(2021), Single))
      isProgressive(QualifiedIncomeBrackets.of(Year.of(2021), HeadOfHousehold))
    }

    "never tax zero gains" >> prop { (ordIncome: TMoney, brackets: QualifiedIncomeBrackets) =>
      brackets.taxDueFunctionally(ordIncome, zero) === zero
    }

    "never tax gains in the lowest (zero-rate) bracket" >> prop {
      brackets: QualifiedIncomeBrackets =>
        val qualifiedIncome = brackets.startOfNonZeroQualifiedRateBracket
        brackets.taxDueFunctionally(zero, qualifiedIncome) === zero
    }

    "tax rises monotonically with qualified income outside the zero bracket" >> prop {
      (brackets: QualifiedIncomeBrackets, gains1: TMoney, gains2: TMoney) =>
        {
          val ordinaryIncome = brackets.startOfNonZeroQualifiedRateBracket
          if (gains1 < gains2)
            brackets.taxDueFunctionally(ordinaryIncome, gains1) < brackets.taxDueFunctionally(
              ordinaryIncome,
              gains2
            )
          else if (gains1 > gains2)
            brackets.taxDueFunctionally(ordinaryIncome, gains1) > brackets.taxDueFunctionally(
              ordinaryIncome,
              gains2
            )
          else
            brackets.taxDueFunctionally(ordinaryIncome, gains1) == brackets.taxDueFunctionally(
              ordinaryIncome,
              gains2
            )
        } must beTrue
    }

    "tax rises monotonically with ordinary income" >> prop {
      (brackets: QualifiedIncomeBrackets, gains: TMoney, income1: TMoney, income2: TMoney) =>
        val res = {
          if (income1 < income2)
            brackets
              .taxDueFunctionally(income1, gains) <= brackets.taxDueFunctionally(income1, gains)
          else if (income1 > income2)
            brackets
              .taxDueFunctionally(income1, gains) >= brackets.taxDueFunctionally(income2, gains)
          else
            brackets.taxDueFunctionally(income1, gains) == brackets.taxDueFunctionally(
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
        res must beTrue
    }

    "tax is never zero except on zero gains, outside the bottom rate" >> prop {
      (brackets: QualifiedIncomeBrackets, gains: TMoney) =>
        val ordinaryIncome = brackets.startOfNonZeroQualifiedRateBracket
        (brackets.taxDueFunctionally(ordinaryIncome, gains).nonZero || gains.isZero) must beTrue
    }

    "max tax rate is the max tax rate" >> prop {
      (brackets: QualifiedIncomeBrackets, gains: TMoney) =>
        val maxTax = gains * brackets.bracketStartsAscending.map(_._2).max
        (brackets.taxDueFunctionally(zero, gains) <= maxTax) must beTrue
    }
  }
}
