package org.kae.ustax4s.federal

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.given
import org.kae.ustax4s.IntMoneySyntax
import org.kae.ustax4s.federal.OrdinaryIncomeBrackets

class OrdinaryIncomeBrackets2018Spec extends FunSuite with IntMoneySyntax:
  private val headOfHouseHoldSamples = List(
    (0, 0),
    (990, 99),
    (13500, 1350),
    (114547, 20389)
  ).map { (income, tax) => (income.asMoney, tax.asMoney) }

  test("TaxBrackets for HOH 2018 should match IRS tables") {
    val brackets = OrdinaryIncomeBrackets.of(Year.of(2018), HeadOfHousehold)
    headOfHouseHoldSamples foreach { (income, tax) =>
      assertEquals(
        brackets.taxDue(income).rounded,
        tax
      )
    }
  }