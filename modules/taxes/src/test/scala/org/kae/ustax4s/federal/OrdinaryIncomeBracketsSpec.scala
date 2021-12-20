package org.kae.ustax4s.federal

import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.OrdinaryIncomeBrackets
import org.kae.ustax4s.money.{Income, MoneyGeneration, TaxPayable}
import org.kae.ustax4s.taxfunction.TaxFunction
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
      TaxFunction.fromBrackets(brackets.thresholds)(Income.zero) == TaxPayable.zero
    }
  }

  property("tax in lowest bracket as expected") {
    forAll { (brackets: OrdinaryIncomeBrackets) =>
      val (lowBracketTop, lowBracketRate) = brackets.thresholdsAscending.head
      TaxFunction.fromBrackets(brackets.thresholds)(
        lowBracketTop.asIncome
      ) == (lowBracketTop.asIncome taxAt lowBracketRate)
    }
  }

  property("tax rises monotonically with income") {
    forAll { (brackets: OrdinaryIncomeBrackets, income1: Income, income2: Income) =>
      val tax = TaxFunction.fromBrackets(brackets.thresholds)
      if income1 < income2 then tax(income1) < tax(income2)
      else if income1 > income2 then tax(income1) > tax(income2)
      else tax(income1) == tax(income2)
    }
  }

  property("tax is never zero except on zero") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: Income) =>
      TaxFunction.fromBrackets(brackets.thresholds)(income).nonZero || income.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: Income) =>
      val maxTax = income taxAt brackets.thresholdsAscending.map(_._2).max
      TaxFunction.fromBrackets(brackets.thresholds)(income) <= maxTax
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

      assertEquals(
        TaxFunction.fromBrackets(brackets.thresholds)(taxableIncome).rounded,
        expectedTax.rounded
      )
  }

  test("inflated brackets incur lower tax for same income") {
    forAll { (brackets: OrdinaryIncomeBrackets, income: Income) =>
      val inflatedBrackets = brackets.inflatedBy(1.2)
      val baseTaxDue       = TaxFunction.fromBrackets(brackets.thresholds)(income)
      val inflatedTaxDue   = TaxFunction.fromBrackets(inflatedBrackets.thresholds)(income)
      inflatedTaxDue <= baseTaxDue
    }
  }
end OrdinaryIncomeBracketsSpec
