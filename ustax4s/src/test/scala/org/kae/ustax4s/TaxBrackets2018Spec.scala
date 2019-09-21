package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.specs2.mutable.Specification

class TaxBrackets2018Spec extends Specification {

  val headOfHouseHoldSamples = List(
    (0, 0),
    (990, 99),
    (13500, 1350),
    (114_547, 20_389)
  ).map { case (income, tax) => (TMoney.u(income), TMoney.u(tax)) }

  "TaxBrackets for HOH 2018" >> {
    "should match IRS tables" >> {
      val brackets = TaxBrackets.of(Year.of(2018), HeadOfHousehold)
      headOfHouseHoldSamples forall { case (income, tax) =>
        brackets.taxDueWholeDollar(income) === tax
      }
    }
  }
}
