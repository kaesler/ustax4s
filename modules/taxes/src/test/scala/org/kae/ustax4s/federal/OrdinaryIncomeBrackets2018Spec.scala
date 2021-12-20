package org.kae.ustax4s.federal

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.federal.OrdinaryIncomeBrackets
import org.kae.ustax4s.money.{Income, TaxPayable}
import org.kae.ustax4s.tax.Tax

class OrdinaryIncomeBrackets2018Spec extends FunSuite:
  private val headOfHouseHoldSamples = List(
    (0, 0),
    (990, 99),
    (13500, 1350),
    (114547, 20389)
  ).map { (income, tax) => (Income(income), TaxPayable(tax)) }

  test("TaxBrackets for HOH 2018 should match IRS tables") {
    val brackets = Trump.ordinaryIncomeBrackets(Year.of(2018), HeadOfHousehold)
    val tax      = Tax.fromBrackets(brackets)
    headOfHouseHoldSamples foreach { (income, expectedTaxDue) =>
      assertEquals(
        tax(income).rounded,
        expectedTaxDue
      )
    }
  }
