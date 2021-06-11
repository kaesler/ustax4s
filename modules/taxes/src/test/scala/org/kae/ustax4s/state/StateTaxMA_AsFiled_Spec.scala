package org.kae.ustax4s.state

import org.kae.ustax4s.given

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.state.StateTaxMA
import org.kae.ustax4s.{IntMoneySyntax, kevin}

class StateTaxMA_AsFiled_Spec extends FunSuite with IntMoneySyntax:

  test("MA State tax should match what I filed in 2018") {

    val incomeBeforeExemptions =
      // Line 19: 5.1% income after deductions
      122210.asMoney +
        // Line 20: interest and dividends
        8958.asMoney +
        // Line 2g: total exemptions (these get removed in calcs)
        7800.asMoney

    assertEquals(
      StateTaxMA
        .taxDue(Year.of(2018), kevin.Kevin.birthDate, HeadOfHousehold, 1)(
          incomeBeforeExemptions
        )
        .rounded,
      6690.asMoney
    )
  }

  test("MA State tax should match what I filed in 2019") {
    val incomeBeforeExemptions =
      // Wages etc, line 10
      148032.asMoney +
        // interest and dividends
        7776.asMoney +
        // Line 2g
        7800.asMoney
    assertEquals(
      StateTaxMA
        .taxDue(Year.of(2019), kevin.Kevin.birthDate, HeadOfHousehold, 1)(
          incomeBeforeExemptions
        )
        .rounded,
      // Note: we ignore the 12% income and cap gains complication
      // here.
      7868.asMoney
    )
  }