package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object SimpleTaxInRetirementSpec extends Specification with MustMatchers {
  import IntMoneySyntax._
  "SimpleTaxInRetirement.ordinaryIncomeTaxDueNoSS" >> {
    "agrees with SimpleTaxInRetirement.taxDueWithSS" >> {
      val year = Year.of(2021)
      for {
        status <- List(HeadOfHousehold, Single)
        i <- 0 to 100000 by 1000
      } {
        import SimpleTaxInRetirement._
        val income = i.tm
        ordinaryIncomeTaxDueNoSS(year, status, income) ===
          taxDueWithSS(year, status, income, TMoney.zero)
      }
      success
    }
  }
}
