package org.kae.ustax4s

import org.scalacheck.Arbitrary
import org.specs2.ScalaCheck
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object TaxBracketsSpec
    extends Specification
    with ScalaCheck
    with TaxBracketsGeneration
    with TMoneyGeneration
    with MustMatchers {

  implicit val arbTaxBrackets: Arbitrary[TaxBrackets] = Arbitrary(
    genTaxBrackets)
  implicit val arbIncome: Arbitrary[TMoney] = Arbitrary(genMoney)

  "TaxBrackets should" >> {
    "never tax zero" >> prop { brackets: TaxBrackets =>
      brackets.taxDue(TMoney.zero) === TMoney.zero
    }

    "tax in lowest bracket as expected" >> prop { brackets: TaxBrackets =>
      val (lowBracketTop, lowBracketRate) = brackets.bracketStartsAscending.head
      brackets.taxDue(lowBracketTop) === lowBracketTop * lowBracketRate
    }

    "tax rises monotonically with income" >> prop {
      (brackets: TaxBrackets, income1: TMoney, income2: TMoney) =>
        {
          if (income1 < income2)
            brackets.taxDue(income1) < brackets.taxDue(income2)
          else if (income1 > income2)
            brackets.taxDue(income1) > brackets.taxDue(income2)
          else brackets.taxDue(income1) == brackets.taxDue(income2)
        } must beTrue
    }

    "tax is never zero except on zero" >> prop {
      (brackets: TaxBrackets, income: TMoney) =>
        (brackets.taxDue(income).nonZero || income.isZero) must beTrue
    }
  }
}
