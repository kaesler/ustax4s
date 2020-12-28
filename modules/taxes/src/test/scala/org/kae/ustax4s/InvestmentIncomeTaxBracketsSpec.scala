package org.kae.ustax4s

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

  implicit val arbCgTaxBrackets: Arbitrary[InvestmentIncomeTaxBrackets] = Arbitrary(genCgTaxBrackets)
  implicit val arbIncome: Arbitrary[TMoney] = Arbitrary(genMoney)

  val zero = TMoney.zero

  "CGTaxBrackets should" >> {
    "never tax zero gains" >> prop { (ordIncome: TMoney ,brackets: InvestmentIncomeTaxBrackets) =>
      brackets.taxDueFunctionally(ordIncome, zero) === zero
    }

    "tax in lowest bracket as expected" >> prop { brackets: InvestmentIncomeTaxBrackets =>
      val (lowBracketTop, lowBracketRate) = brackets.bracketStartsAscending.head
      brackets.taxDueFunctionally(zero, lowBracketTop) === lowBracketTop * lowBracketRate
    }

    "tax rises monotonically with investment income" >> prop {
      (brackets: InvestmentIncomeTaxBrackets, gains1: TMoney, gains2: TMoney) =>
        {
          if (gains1 < gains2)
            brackets.taxDueFunctionally(zero, gains1) < brackets.taxDueFunctionally(zero, gains2)
          else if (gains1 > gains2)
            brackets.taxDueFunctionally(zero, gains1) > brackets.taxDueFunctionally(zero, gains2)
          else brackets.taxDueFunctionally(zero, gains1) == brackets.taxDueFunctionally(zero, gains2)
        } must beTrue
    }

    "tax rises monotonically with ordinary income" >> prop {
      (brackets: InvestmentIncomeTaxBrackets, gains: TMoney, income1: TMoney, income2: TMoney) =>
        val res = {
          if (income1 < income2)
            brackets.taxDueFunctionally(income1, gains) <= brackets.taxDueFunctionally(income1, gains)
          else if (income1 > income2)
            brackets.taxDueFunctionally(income1, gains) >= brackets.taxDueFunctionally(income2, gains)
          else brackets.taxDueFunctionally(income1, gains) == brackets.taxDueFunctionally(income2, gains)
        }
        if (!res) {
          println(brackets.show)
          println(s"gains: $gains")
          println(s"income1: $income1; tax: ${brackets.taxDueFunctionally(income1, gains)}")
          println(s"income2: $income2; tax: ${brackets.taxDueFunctionally(income2, gains)}")
        }
        res must beTrue
    }

    "tax is never zero except on zero gains" >> prop {
      (brackets: InvestmentIncomeTaxBrackets, income: TMoney, gains: TMoney) =>
        (brackets.taxDueFunctionally(income, gains).nonZero || gains.isZero) must beTrue
    }

    "max tax rate is the max tax rate" >> prop {
      (brackets: InvestmentIncomeTaxBrackets, gains: TMoney) =>
        val maxTax = gains * brackets.bracketStartsAscending.map(_._2).max
        (brackets.taxDueFunctionally(zero, gains) <= maxTax) must beTrue
    }
  }
}
