package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.specs2.mutable.Specification

object QualifiedIncomeBrackets2018Spec extends Specification {

  "InvestmentIncomeTaxBrackets for HOH 2018" >> {
    "should match my actual return" >> {
      val brackets = QualifiedIncomeBrackets.of(Year.of(2018), HeadOfHousehold)
      brackets
        .taxDueFunctionally(
          TMoney.u(114547),
          TMoney.u(14777)
        )
        .rounded === TMoney.u(2217)
    }
  }
}
