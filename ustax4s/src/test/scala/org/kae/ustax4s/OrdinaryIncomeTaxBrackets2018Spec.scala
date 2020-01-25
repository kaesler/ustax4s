package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.specs2.mutable.Specification

object OrdinaryIncomeTaxBrackets2018Spec extends Specification with IntMoneySyntax {
  val zero = TMoney.zero
  val headOfHouseHoldSamples = List(
    (0, 0),
    (990, 99),
    (13500, 1350),
    (114547, 20389)
  ).map { case (income, tax) => (income.tm, tax.tm) }

  "TaxBrackets for HOH 2018" >> {
    "should match IRS tables" >> {
      val brackets = OrdinaryIncomeTaxBrackets.of(Year.of(2018), HeadOfHousehold)
      headOfHouseHoldSamples forall { case (income, tax) =>
        brackets.taxDue(income).rounded === tax
      }
    }
  }
}
