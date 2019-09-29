package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.specs2.mutable.Specification

class CGTaxBrackets2018Spec extends Specification {

  "CGTaxBrackets for HOH 2018" >> {
    "should match my actual return" >> {
      val brackets = CGTaxBrackets.of(Year.of(2018), HeadOfHousehold)
        brackets.taxDueWholeDollar(
          TMoney.u(114547),
          TMoney.u(14777)
          ) === TMoney.u(2217)
    }
  }
}
