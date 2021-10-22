package org.kae.ustax4s.state_ma

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*

class SmokeTestAgainstMyMA2018Return extends FunSuite:

  test("MA State tax should match what I filed in 2018") {

    val incomeBeforeExemptions =
      // Line 19: 5.1% income after deductions
      122210.asMoney +
        // Line 20: interest and dividends
        8958.asMoney +
        // Line 2g: total exemptions (these get removed in calcs)
        7800.asMoney

    assertEquals(
      StateMATaxCalculator
        .taxDue(Year.of(2018), Kevin.birthDate, 1, HeadOfHousehold)(
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
      StateMATaxCalculator
        .taxDue(Year.of(2019), Kevin.birthDate, 1, HeadOfHousehold)(
          incomeBeforeExemptions
        )
        .rounded,
      // Note: we ignore the 12% income and cap gains complication
      // here.
      7868.asMoney
    )
  }