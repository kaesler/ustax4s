package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.specs2.mutable.Specification

object StateTaxMA_AsFiled_Spec extends Specification with IntMoneySyntax {

  "MA State tax" >> {
    "should match what I filed in 2018" >> {

      val incomeBeforeExemptions =
        // Wages etc, line 18
        122210.tm +
          // interest and dividends
          8958.tm +
          // Line 2g
          7800.tm
      StateTaxMA.taxDue(
        Year.of(2018),
        HeadOfHousehold,
        Kevin.birthDate,
        1)(
        incomeBeforeExemptions
      ).rounded === 6690.tm
    }
  }

  "should match what I filed in 2019" >> {

      val incomeBeforeExemptions =
        // Wages etc, line 10
        148032.tm +
          // interest and dividends
          7776.tm +
          // Line 2g
          7800.tm
      StateTaxMA.taxDue(
        Year.of(2018),
        HeadOfHousehold,
        Kevin.birthDate,
        1)(
        incomeBeforeExemptions
      ).rounded === 7918.tm
  }
}
