package org.kae.ustax4s

import org.scalacheck.Arbitrary
import org.specs2.ScalaCheck
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object OrdinaryIncomeTaxBracketsSpec
    extends Specification
    with ScalaCheck
    with TaxBracketsGeneration
    with TMoneyGeneration
    with MustMatchers {

  implicit val arbTaxBrackets: Arbitrary[OrdinaryIncomeTaxBrackets] = Arbitrary(
    genTaxBrackets)
  implicit val arbIncome: Arbitrary[TMoney] = Arbitrary(genMoney)

  val zero = TMoney.zero

  "TaxBrackets should" >> {
    "never tax zero" >> prop { brackets: OrdinaryIncomeTaxBrackets =>
      brackets.taxDue(TMoney.zero) === TMoney.zero
    }

    "tax in lowest bracket as expected" >> prop { brackets: OrdinaryIncomeTaxBrackets =>
      val (lowBracketTop, lowBracketRate) = brackets.bracketStartsAscending.head
      brackets.taxDue(lowBracketTop) === lowBracketTop * lowBracketRate
    }

    "tax rises monotonically with income" >> prop {
      (brackets: OrdinaryIncomeTaxBrackets, income1: TMoney, income2: TMoney) =>
        {
          if (income1 < income2)
            brackets.taxDue(income1) < brackets.taxDue(income2)
          else if (income1 > income2)
            brackets.taxDue(income1) > brackets.taxDue(income2)
          else brackets.taxDue(income1) == brackets.taxDue(income2)
        } must beTrue
    }

    "tax is never zero except on zero" >> prop {
      (brackets: OrdinaryIncomeTaxBrackets, income: TMoney) =>
        (brackets.taxDue(income).nonZero || income.isZero) must beTrue
    }

    "max tax rate is the max tax rate" >> prop {
      (brackets: OrdinaryIncomeTaxBrackets, income: TMoney) =>
        val maxTax = income * brackets.bracketStartsAscending.map(_._2).max
        (brackets.taxDue(income) <= maxTax) must beTrue
    }
  }
}
