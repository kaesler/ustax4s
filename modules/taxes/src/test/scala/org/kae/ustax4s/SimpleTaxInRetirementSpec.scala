package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object SimpleTaxInRetirementSpec extends Specification with MustMatchers {
  import IntMoneySyntax._

  "SimpleTaxInRetirement.taxDue" >> {
    "agrees with SimpleTaxInRetirement.taxDueUsingForm1040" >> {
      val year = Year.of(2021)
      for {
        status <- List(HeadOfHousehold, Single)
        i <- 0 to 100000 by 500
        ss <-  0 to 49000 by 500
      } {
        import SimpleTaxInRetirement._
        val income = i.tm
        val socialSecurity = ss.tm

        taxDue(year, status, income, socialSecurity) ===
          taxDueUsingForm1040(year, status, income, socialSecurity)
      }
      success
    }
  }
}
