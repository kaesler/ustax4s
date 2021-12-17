package org.kae.ustax4s.federal

import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.OrdinaryIncomeBrackets
import org.kae.ustax4s.money.MoneyGeneration
import org.kae.ustax4s.money.{Income, TaxPayable}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

class OrdinaryIncomeBracketsSpec
    extends ScalaCheckSuite
    with TaxBracketsGeneration
    with MoneyGeneration:

  // import math.Ordered.orderingToOrdered
  import math.Ordering.Implicits.infixOrderingOps

  given Arbitrary[OrdinaryIncomeBrackets] = Arbitrary(genTaxBrackets)

  given Arbitrary[Income] = Arbitrary(genIncome)

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
      TaxPayable(1420)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.12)).rounded,
      TaxPayable(6220)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.22)).rounded,
      TaxPayable(13293)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.24)).rounded,
      TaxPayable(32145)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.32)).rounded,
      TaxPayable(46385)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.35)).rounded,
      TaxPayable(156355)
    )
  }

  test("taxToEndOfBracket should be correct for 2021 Single") {
    val brackets = bracketsFor(TheYear, Single)
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.10)).rounded,
      TaxPayable(995)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.12)).rounded,
      TaxPayable(4664)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.22)).rounded,
      TaxPayable(14751)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.24)).rounded,
      TaxPayable(33603)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.32)).rounded,
      TaxPayable(47843)
    )
    assertEquals(
      brackets.taxToEndOfBracket(FederalTaxRate.unsafeFrom(0.35)).rounded,
      TaxPayable(157804)
    )
  }

  property("never tax zero") {
    forAll { (brackets: OrdinaryIncomeBrackets) =>
      brackets.taxDue(Income.zero) == TaxPayable.zero
    }
  }

  property("tax in lowest bracket as expected") {
    forAll { (brackets: OrdinaryIncomeBrackets) =>
      val (lowBracketTop, lowBracketRate) = brackets.bracketStartsAscending.head
      brackets.taxDue(lowBracketTop.asIncome) == (lowBracketTop.asIncome taxAt lowBracketRate)
    }
  }

  property("tax rises monotonically with income") {
    forAll { (brackets: OrdinaryIncomeBrackets, income1: Income, income2: Income) =>
      if income1 < income2 then brackets.taxDue(income1) < brackets.taxDue(income2)
      else if income1 > income2 then brackets.taxDue(income1) > brackets.taxDue(income2)
      else brackets.taxDue(income1) == brackets.taxDue(income2)
    }
  }

  property("tax is never zero except on zero") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: Income) =>
      brackets.taxDue(income).nonZero || income.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: Income) =>
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

  test("inflated brackets incur lower tax for same income") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: Income) =>
      val inflatedBrackets = brackets.inflatedBy(1.2)
      val baseTaxDue       = brackets.taxDue(income)
      val inflatedTaxDue   = inflatedBrackets.taxDue(income)
      inflatedTaxDue <= baseTaxDue
    }
  }
end OrdinaryIncomeBracketsSpec
