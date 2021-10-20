package org.kae.ustax4s.federal

import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.OrdinaryIncomeBrackets
import org.kae.ustax4s.money.{Money, MoneyGeneration}
import org.kae.ustax4s.money.MoneySyntax.*
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

class OrdinaryIncomeBracketsSpec
    extends ScalaCheckSuite
    with TaxBracketsGeneration
    with MoneyGeneration:

  given Arbitrary[OrdinaryIncomeBrackets] = Arbitrary(genTaxBrackets)

  given Arbitrary[Money] = Arbitrary(genMoney)

  private val zero    = Money.zero
  private val TheYear = Year.of(2021)

  private def bracketsFor(year: Year, filingStatus: FilingStatus) =
    Trump.ordinaryIncomeBrackets(year, filingStatus)

  test("OrdinaryIncomeTaxBrackets should be progressive") {

    def isProgressive(brackets: OrdinaryIncomeBrackets): Boolean = {
      val rates = brackets.bracketStartsAscending.map(_._2)
      (rates zip rates.tail)
        .forall { (left, right) =>
          left < right
        }
    }

    assert(isProgressive(bracketsFor(TheYear, Single)))
    assert(isProgressive(bracketsFor(TheYear, HeadOfHousehold)))
  }

  test("taxToEndOfBracket should be correct for 2021 HeadOfHousehold") {
    val brackets = bracketsFor(TheYear, HeadOfHousehold)

    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.10)).rounded,
      1420.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.12)).rounded,
      6220.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.22)).rounded,
      13293.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.24)).rounded,
      32145.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.32)).rounded,
      46385.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.35)).rounded,
      156355.asMoney
    )
  }

  test("taxToEndOfBracket should be correct for 2021 Single") {
    val brackets = bracketsFor(TheYear, Single)
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.10)).rounded,
      995.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.12)).rounded,
      4664.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.22)).rounded,
      14751.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.24)).rounded,
      33603.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.32)).rounded,
      47843.asMoney
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.35)).rounded,
      157804.asMoney
    )
  }

  property("never tax zero") {
    forAll { (brackets: OrdinaryIncomeBrackets) =>
      brackets.taxDue(Money.zero) == zero
    }
  }

  property("tax in lowest bracket as expected") {
    forAll { (brackets: OrdinaryIncomeBrackets) =>
      val (lowBracketTop, lowBracketRate) = brackets.bracketStartsAscending.head
      brackets.taxDue(lowBracketTop) == (lowBracketTop taxAt lowBracketRate)
    }
  }

  property("tax rises monotonically with income") {
    forAll { (brackets: OrdinaryIncomeBrackets, income1: Money, income2: Money) =>
      if income1 < income2 then brackets.taxDue(income1) < brackets.taxDue(income2)
      else if income1 > income2 then brackets.taxDue(income1) > brackets.taxDue(income2)
      else brackets.taxDue(income1) == brackets.taxDue(income2)
    }
  }

  property("tax is never zero except on zero") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: Money) =>
      brackets.taxDue(income).nonZero || income.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: Money) =>
      val maxTax = income taxAt brackets.bracketStartsAscending.map(_._2).max
      brackets.taxDue(income) <= maxTax
    }
  }

  test("give expected results at bracket boundaries for 2021") {
    for
      filingStatus <- List(Single)
      brackets = bracketsFor(TheYear, filingStatus)
      rate <- brackets.ratesForBoundedBrackets
    do
      val taxableIncome = brackets.taxableIncomeToEndOfBracket(rate)
      val expectedTax   = brackets.taxToEndOfBracket(rate)

      assertEquals(brackets.taxDue(taxableIncome), expectedTax)
  }
