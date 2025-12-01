package org.kae.ustax4s.federal

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.TaxFunction
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.{TaxPayable, TaxableIncome}

class OrdinaryRateFunction2018Spec extends FunSuite:
  private val headOfHouseHoldSamples = List(
    (0, 0),
    (990, 99),
    (13500, 1350),
    (114547, 20389)
  ).map { (income, tax) => (TaxableIncome(income), TaxPayable(tax)) }

  test("TaxBrackets for HOH 2018 should match IRS tables") {
    val orf = YearlyValues
      .of(Year.of(2018))
      .get
      .ordinaryRateFunctions(HeadOfHousehold)
    val tax = TaxFunction.fromRateFunction(orf.rateFunction)
    headOfHouseHoldSamples foreach { (income, expectedTaxDue) =>
      assertEquals(
        tax(income).rounded,
        expectedTaxDue
      )
    }
  }
