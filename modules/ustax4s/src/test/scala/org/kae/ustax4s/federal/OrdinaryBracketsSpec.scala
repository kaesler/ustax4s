package org.kae.ustax4s.federal

import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.OrdinaryBrackets
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.{Income, MoneyGeneration, TaxPayable, TaxableIncome}
import org.kae.ustax4s.taxfunction.TaxFunction
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import scala.math.Ordering.Implicits.infixOrderingOps

class OrdinaryBracketsSpec extends ScalaCheckSuite with TaxBracketsGeneration with MoneyGeneration:

  // import math.Ordered.orderingToOrdered

  given Arbitrary[OrdinaryBrackets] = Arbitrary(genTaxBrackets)

  given Arbitrary[TaxableIncome] = Arbitrary(genTaxableIncome)

  private val TheYear = Year.of(2021)

  private def bracketsFor(year: Year, filingStatus: FilingStatus) =
    YearlyValues.of(year).get.ordinaryBrackets(filingStatus)

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
    forAll { (brackets: OrdinaryBrackets) =>
      TaxFunction.fromBrackets(brackets.brackets)(TaxableIncome.zero) == TaxPayable.zero
    }
  }

  property("tax in lowest bracket as expected") {
    forAll { (brackets: OrdinaryBrackets) =>
      val (lowBracketTop, lowBracketRate) = brackets.bracketsAscending.head
      TaxFunction.fromBrackets(brackets.brackets)(
        lowBracketTop
      ) == (lowBracketTop taxAt lowBracketRate)
    }
  }

  property("tax rises monotonically with income") {
    forAll { (brackets: OrdinaryBrackets, income1: TaxableIncome, income2: TaxableIncome) =>
      given Ordering[TaxableIncome] with
        def compare(x: TaxableIncome, y: TaxableIncome): Int =
          summon[Ordering[Income]].compare(x, y)
      val tax = TaxFunction.fromBrackets(brackets.brackets)
      if income1 < income2 then tax(income1) < tax(income2)
      else if income1 > income2 then tax(income1) > tax(income2)
      else tax(income1) == tax(income2)
    }
  }

  property("tax is never zero except on zero") {
    forAll { (brackets: OrdinaryBrackets, income: TaxableIncome) =>
      TaxFunction.fromBrackets(brackets.brackets)(income).nonZero || income.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (brackets: OrdinaryBrackets, income: TaxableIncome) =>
      val maxTax = income taxAt brackets.bracketsAscending.map(_._2).max
      TaxFunction.fromBrackets(brackets.brackets)(income) <= maxTax
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
        TaxFunction.fromBrackets(brackets.brackets)(taxableIncome).rounded,
        expectedTax.rounded
      )
  }

  test("inflated brackets incur lower tax for same income") {
    forAll { (brackets: OrdinaryBrackets, income: TaxableIncome) =>
      val inflatedBrackets = brackets.inflatedBy(1.2)
      val baseTaxDue       = TaxFunction.fromBrackets(brackets.brackets)(income)
      val inflatedTaxDue   = TaxFunction.fromBrackets(inflatedBrackets.brackets)(income)
      inflatedTaxDue <= baseTaxDue
    }
  }
end OrdinaryBracketsSpec
