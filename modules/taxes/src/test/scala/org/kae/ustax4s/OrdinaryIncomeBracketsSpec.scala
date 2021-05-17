package org.kae.ustax4s

import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

class OrdinaryIncomeBracketsSpec
    extends ScalaCheckSuite
    with TaxBracketsGeneration
    with TMoneyGeneration
    with IntMoneySyntax {

  implicit val arbTaxBrackets: Arbitrary[OrdinaryIncomeBrackets] = Arbitrary(genTaxBrackets)
  implicit val arbIncome: Arbitrary[TMoney]                      = Arbitrary(genMoney)

  private val zero    = TMoney.zero
  private val TheYear = Year.of(2021)

  test("OrdinaryIncomeTaxBrackets should be progressive") {

    def isProgressive(brackets: OrdinaryIncomeBrackets): Boolean = {
      val rates = brackets.bracketStartsAscending.map(_._2)
      (rates zip rates.tail)
        .forall { case (left, right) =>
          left < right
        }
    }

    assert(isProgressive(OrdinaryIncomeBrackets.of(TheYear, Single)))
    assert(isProgressive(OrdinaryIncomeBrackets.of(TheYear, HeadOfHousehold)))
  }

  test("taxToEndOfBracket should be correct for 2021 HeadOfHousehold") {
    val brackets = OrdinaryIncomeBrackets.of(TheYear, HeadOfHousehold)

    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.10)).rounded,
      1420.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.12)).rounded,
      6220.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.22)).rounded,
      13293.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.24)).rounded,
      32145.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.32)).rounded,
      46385.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.35)).rounded,
      156355.tm
    )
  }

  test("taxToEndOfBracket should be correct for 2021 Single") {
    val brackets = OrdinaryIncomeBrackets.of(TheYear, Single)
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.10)).rounded,
      995.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.12)).rounded,
      4664.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.22)).rounded,
      14751.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.24)).rounded,
      33603.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.32)).rounded,
      47843.tm
    )
    assertEquals(
      brackets.taxToEndOfBracket(TaxRate.unsafeFrom(0.35)).rounded,
      157804.tm
    )
  }

  property("never tax zero") {
    forAll { brackets: OrdinaryIncomeBrackets =>
      brackets.taxDue(TMoney.zero) == zero
    }
  }

  property("tax in lowest bracket as expected") {
    forAll { brackets: OrdinaryIncomeBrackets =>
      val (lowBracketTop, lowBracketRate) = brackets.bracketStartsAscending.head
      brackets.taxDue(lowBracketTop) == lowBracketTop * lowBracketRate
    }
  }

  property("tax rises monotonically with income") {
    forAll { (brackets: OrdinaryIncomeBrackets, income1: TMoney, income2: TMoney) =>
      if (income1 < income2)
        brackets.taxDue(income1) < brackets.taxDue(income2)
      else if (income1 > income2)
        brackets.taxDue(income1) > brackets.taxDue(income2)
      else brackets.taxDue(income1) == brackets.taxDue(income2)
    }
  }

  property("tax is never zero except on zero") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: TMoney) =>
      brackets.taxDue(income).nonZero || income.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: TMoney) =>
      val maxTax = income * brackets.bracketStartsAscending.map(_._2).max
      brackets.taxDue(income) <= maxTax
    }
  }

  test("give expected results at bracket boundaries for 2021") {
    for {
      filingStatus <- List(Single)
      brackets = OrdinaryIncomeBrackets.of(TheYear, filingStatus)
      rate <- brackets.ratesForBoundedBrackets
    } {
      val taxableIncome = brackets.taxableIncomeToEndOfBracket(rate)
      val expectedTax   = brackets.taxToEndOfBracket(rate)

      assertEquals(brackets.taxDue(taxableIncome), expectedTax)
    }
  }
}
