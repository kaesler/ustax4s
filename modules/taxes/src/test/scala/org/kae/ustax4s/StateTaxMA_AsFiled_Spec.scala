package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.specs2.mutable.Specification

object StateTaxMA_AsFiled_Spec extends Specification with IntMoneySyntax {
  "MA State tax" >> {
    "should match what I filed in 2018" >> {

      val incomeBeforeExemptions =
        // Wages etc, line 10
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
}
