package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.scalacheck.Arbitrary
import org.specs2.ScalaCheck
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object OrdinaryIncomeBracketsSpec
    extends Specification
    with ScalaCheck
    with TaxBracketsGeneration
    with TMoneyGeneration
    with MustMatchers
    with IntMoneySyntax {

  implicit val arbTaxBrackets: Arbitrary[OrdinaryIncomeBrackets] = Arbitrary(genTaxBrackets)
  implicit val arbIncome: Arbitrary[TMoney]                      = Arbitrary(genMoney)

  private val zero    = TMoney.zero
  private val TheYear = Year.of(2021)

  "OrdinaryIncomeTaxBrackets should" >> {

    "be progressive" >> {

      def isProgressive(brackets: OrdinaryIncomeBrackets): Boolean = {
        val rates = brackets.bracketStartsAscending.map(_._2)
        (rates zip rates.tail)
          .forall { case (left, right) =>
            left < right
          }
      }

      isProgressive(OrdinaryIncomeBrackets.of(TheYear, Single))
      isProgressive(OrdinaryIncomeBrackets.of(TheYear, HeadOfHousehold))
    }

    "taxToEndOfBracket" >> {
      "should be correct for 2021 HeadOfHousehold" >> {
        val brackets = OrdinaryIncomeBrackets.of(TheYear, HeadOfHousehold)
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.10)).rounded === 1420.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.12)).rounded === 6220.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.22)).rounded === 13293.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.24)).rounded === 32145.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.32)).rounded === 46385.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.35)).rounded === 156355.tm
      }
      "should be correct for 2021 Single" >> {
        val brackets = OrdinaryIncomeBrackets.of(TheYear, Single)
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.10)).rounded === 995.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.12)).rounded === 4664.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.22)).rounded === 14751.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.24)).rounded === 33603.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.32)).rounded === 47843.tm
        brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.35)).rounded === 157804.tm
      }
    }

    "never tax zero" >> prop { brackets: OrdinaryIncomeBrackets =>
      brackets.taxDue(TMoney.zero) === zero
    }

    "tax in lowest bracket as expected" >> prop { brackets: OrdinaryIncomeBrackets =>
      val (lowBracketTop, lowBracketRate) = brackets.bracketStartsAscending.head
      brackets.taxDue(lowBracketTop) === lowBracketTop * lowBracketRate
    }

    "tax rises monotonically with income" >> prop {
      (brackets: OrdinaryIncomeBrackets, income1: TMoney, income2: TMoney) =>
        {
          if (income1 < income2)
            brackets.taxDue(income1) < brackets.taxDue(income2)
          else if (income1 > income2)
            brackets.taxDue(income1) > brackets.taxDue(income2)
          else brackets.taxDue(income1) == brackets.taxDue(income2)
        } must beTrue
    }

    "tax is never zero except on zero" >> prop {
      (brackets: OrdinaryIncomeBrackets, income: TMoney) =>
        (brackets.taxDue(income).nonZero || income.isZero) must beTrue
    }

    "max tax rate is the max tax rate" >> prop {
      (brackets: OrdinaryIncomeBrackets, income: TMoney) =>
        val maxTax = income * brackets.bracketStartsAscending.map(_._2).max
        (brackets.taxDue(income) <= maxTax) must beTrue
    }

    "give expected results at bracket boundaries for 2021" >> {
      for {
        filingStatus <- List(Single)
        brackets = OrdinaryIncomeBrackets.of(TheYear, filingStatus)
        rate <- brackets.ratesForBoundedBrackets
      } {
        val taxableIncome = brackets.taxableIncomeToEndOfBracket(rate)
        val expectedTax   = brackets.taxToEndOfBracket(rate)
        brackets.taxDue(taxableIncome) === expectedTax
      }
      ok
    }
  }
}
