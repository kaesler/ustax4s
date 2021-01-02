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
        ss <- 0 to 49000 by 500
      } {
        import SimpleTaxInRetirement._
        val income = i.tm
        val socialSecurity = ss.tm

        taxDueNoQualifiedInvestments(
          year = year,
          filingStatus = status,
          incomeFrom401kEtc = income,
          socSec = socialSecurity
        ) ===
          taxDueUsingForm1040(
            year = year,
            filingStatus = status,
            socSec = socialSecurity,
            incomeFrom401k = income,
            qualifiedDividends = 0.tm
          )
      }
      success
    }
  }
  "SimpleTaxInRetirement.taxDueWithInvestments" >> {
    "agrees with SimpleTaxInRetirement.taxDueUsingForm1040" >> {
      val year = Year.of(2021)
      for {
        status <- List(HeadOfHousehold, Single)
        i <- 0 to 70000 by 1000
        ss <- 0 to 49000 by 1000
        inv <- 0 to 30000 by 1000
      } {
        import SimpleTaxInRetirement._
        val income = i.tm
        val socialSecurity = ss.tm
        val qualifiedDividends = inv.tm

        if (taxDue(
          year = year,
          filingStatus = status,
          socSec = socialSecurity,
          incomeFrom401kEtc = income,
          qualifiedInvestmentIncome = qualifiedDividends
            ) !=
              taxDueUsingForm1040(
                year = year,
                filingStatus = status,
                socSec = socialSecurity,
                incomeFrom401k = income,
                qualifiedDividends = qualifiedDividends
              )) {
          println(s"status: $status; i: $i; ss: $ss; inv: $inv")
        }
        taxDue(
          year = year,
          filingStatus = status,
          socSec = socialSecurity,
          incomeFrom401kEtc = income,
          qualifiedInvestmentIncome = qualifiedDividends
        ) ===
          taxDueUsingForm1040(
            year = year,
            filingStatus = status,
            socSec = socialSecurity,
            incomeFrom401k = income,
            qualifiedDividends = qualifiedDividends
          )
      }
      success
    }
  }
}
