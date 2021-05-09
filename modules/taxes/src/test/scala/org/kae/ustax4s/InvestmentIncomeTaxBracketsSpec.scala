package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.scalacheck.Arbitrary
import org.specs2.ScalaCheck
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object InvestmentIncomeTaxBracketsSpec
    extends Specification
    with ScalaCheck
    with InvestmentTaxBracketsGeneration
    with TMoneyGeneration
    with MustMatchers {

  private implicit val arbInvestmentTaxBrackets: Arbitrary[InvestmentIncomeTaxBrackets] = Arbitrary(
    genInvestmentTaxBrackets
  )
  private implicit val arbIncome: Arbitrary[TMoney] = Arbitrary(genMoney)

  private val zero = TMoney.zero

  "InvestmentIncomeTaxBrackets should" >> {

    "be progressive" >> {
      def isProgressive(brackets: InvestmentIncomeTaxBrackets): Boolean = {
        val rates = brackets.bracketStartsAscending.map(_._2)
        (rates zip rates.tail)
          .forall { case (left, right) =>
            left < right
          }
      }

      isProgressive(InvestmentIncomeTaxBrackets.of(Year.of(2021), Single))
      isProgressive(InvestmentIncomeTaxBrackets.of(Year.of(2021), HeadOfHousehold))
    }

    "never tax zero gains" >> prop { (ordIncome: TMoney, brackets: InvestmentIncomeTaxBrackets) =>
      brackets.taxDueFunctionally(ordIncome, zero) === zero
    }

    "never tax gains in the lowest (zero-rate) bracket" >> prop {
      brackets: InvestmentIncomeTaxBrackets =>
        val investmentIncome = brackets.startOfNonZeroRateBracket
        brackets.taxDueFunctionally(zero, investmentIncome) === zero
    }

    "tax rises monotonically with investment income outside the zero bracket" >> prop {
      (brackets: InvestmentIncomeTaxBrackets, gains1: TMoney, gains2: TMoney) =>
        {
          val ordinaryIncome = brackets.startOfNonZeroRateBracket
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
      (brackets: InvestmentIncomeTaxBrackets, gains: TMoney, income1: TMoney, income2: TMoney) =>
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
      (brackets: InvestmentIncomeTaxBrackets, gains: TMoney) =>
        val ordinaryIncome = brackets.startOfNonZeroRateBracket
        (brackets.taxDueFunctionally(ordinaryIncome, gains).nonZero || gains.isZero) must beTrue
    }

    "max tax rate is the max tax rate" >> prop {
      (brackets: InvestmentIncomeTaxBrackets, gains: TMoney) =>
        val maxTax = gains * brackets.bracketStartsAscending.map(_._2).max
        (brackets.taxDueFunctionally(zero, gains) <= maxTax) must beTrue
    }
  }
}
