package org.kae.ustax4s.federal

import java.time.Year
import munit.ScalaCheckSuite
import org.kae.ustax4s.{FilingStatus, TaxFunction}
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.{Income, MoneyGeneration, TaxPayable, TaxableIncome}
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import scala.math.Ordering.Implicits.infixOrderingOps

class OrdinaryRateFunctionSpec
    extends ScalaCheckSuite
    with TaxBracketsGeneration
    with MoneyGeneration:

  // import math.Ordered.orderingToOrdered

  given Arbitrary[OrdinaryRateFunction] = Arbitrary(genTaxBrackets)

  given Arbitrary[TaxableIncome] = Arbitrary(genTaxableIncome)

  private val TheYear = Year.of(2021)

  private def ordinaryRateFunctionFor(year: Year, filingStatus: FilingStatus) =
    YearlyValues.of(year).get.ordinaryRateFunctions(filingStatus)

  test("taxToEndOfBracket should be correct for 2021 HeadOfHousehold") {
    val orf = ordinaryRateFunctionFor(TheYear, HeadOfHousehold)

    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.10)).rounded,
      TaxPayable(1420)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.12)).rounded,
      TaxPayable(6220)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.22)).rounded,
      TaxPayable(13293)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.24)).rounded,
      TaxPayable(32145)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.32)).rounded,
      TaxPayable(46385)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.35)).rounded,
      TaxPayable(156355)
    )
  }

  test("taxToEndOfBracket should be correct for 2021 Single") {
    val orf = ordinaryRateFunctionFor(TheYear, Single)
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.10)).rounded,
      TaxPayable(995)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.12)).rounded,
      TaxPayable(4664)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.22)).rounded,
      TaxPayable(14751)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.24)).rounded,
      TaxPayable(33603)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.32)).rounded,
      TaxPayable(47843)
    )
    assertEquals(
      orf.unsafeTaxToEndOfBracket(FedTaxRate.unsafeFrom(0.35)).rounded,
      TaxPayable(157804)
    )
  }

  property("never tax zero") {
    forAll { (orf: OrdinaryRateFunction) =>
      TaxFunction.fromRateFunction(orf.rateFunction)(TaxableIncome.zero) == TaxPayable.zero
    }
  }

  property("tax in lowest bracket as expected") {
    forAll { (orf: OrdinaryRateFunction) =>
      val (lowBracketTop, lowBracketRate) = orf.bracketsAscending.head
      TaxFunction.fromRateFunction(orf.rateFunction)(
        lowBracketTop
      ) == (lowBracketTop taxAt lowBracketRate)
    }
  }

  property("tax rises monotonically with income") {
    forAll { (orf: OrdinaryRateFunction, income1: TaxableIncome, income2: TaxableIncome) =>
      given Ordering[TaxableIncome]:
        def compare(x: TaxableIncome, y: TaxableIncome): Int =
          summon[Ordering[Income]].compare(x, y)
      val tax = TaxFunction.fromRateFunction(orf.rateFunction)
      if income1 < income2 then tax(income1) < tax(income2)
      else if income1 > income2 then tax(income1) > tax(income2)
      else tax(income1) == tax(income2)
    }
  }

  property("tax is never zero except on zero") {
    forAll { (orf: OrdinaryRateFunction, income: TaxableIncome) =>
      TaxFunction.fromRateFunction(orf.rateFunction)(income).nonZero || income.isZero
    }
  }

  property("max tax rate is the max tax rate") {
    forAll { (orf: OrdinaryRateFunction, income: TaxableIncome) =>
      val maxTax = income taxAt orf.bracketsAscending.map(_.rate).max
      TaxFunction.fromRateFunction(orf.rateFunction)(income) <= maxTax
    }
  }

  test("give expected results at bracket boundaries for 2021") {
    for
      filingStatus <- List(Single)
      orf = ordinaryRateFunctionFor(TheYear, filingStatus)
      rate <- orf.ratesForBoundedBrackets
    do
      val taxableIncome = orf.unsafeTaxableIncomeToEndOfBracket(rate)
      val expectedTax   = orf.unsafeTaxToEndOfBracket(rate)

      assertEquals(
        TaxFunction.fromRateFunction(orf.rateFunction)(taxableIncome).rounded,
        expectedTax.rounded
      )
  }

  test("inflated rate functions incur lower tax for same income") {
    forAll { (orf: OrdinaryRateFunction, income: TaxableIncome) =>
      val inflatedOrf = orf.inflatedBy(1.2)
      val baseTaxDue       = TaxFunction.fromRateFunction(orf.rateFunction)(income)
      val inflatedTaxDue   = TaxFunction.fromRateFunction(inflatedOrf.rateFunction)(income)
      inflatedTaxDue <= baseTaxDue
    }
  }
end OrdinaryRateFunctionSpec
