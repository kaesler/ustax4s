package org.kae.ustax4s

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold

class StateTaxMA_AsFiled_Spec extends FunSuite with IntMoneySyntax {

  test("MA State tax should match what I filed in 2018") {

    val incomeBeforeExemptions =
      // Line 19: 5.1% income after deductions
      122210.tm +
        // Line 20: interest and dividends
        8958.tm +
        // Line 2g: total exemptions (these get removed in calcs)
        7800.tm

    assertEquals(
      StateTaxMA
        .taxDue(Year.of(2018), HeadOfHousehold, Kevin.birthDate, 1)(
          incomeBeforeExemptions
        )
        .rounded,
      6690.tm
    )
  }

  test("MA State tax should match what I filed in 2019") {
    val incomeBeforeExemptions =
      // Wages etc, line 10
      148032.tm +
        // interest and dividends
        7776.tm +
        // Line 2g
        7800.tm
    assertEquals(
      StateTaxMA
        .taxDue(Year.of(2018), HeadOfHousehold, Kevin.birthDate, 1)(
          incomeBeforeExemptions
        )
        .rounded,
      7918.tm
    )
  }
}
