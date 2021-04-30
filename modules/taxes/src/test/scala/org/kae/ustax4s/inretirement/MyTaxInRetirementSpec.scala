package org.kae.ustax4s.inretirement

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object MyTaxInRetirementSpec extends Specification with MustMatchers {

  "MyTaxInRetirement.taxDue" >> {
    "agrees with MyTaxInRetirement.taxDueUsingForm1040" >> {
      val year = Year.of(2021)
      for {
        status <- List(HeadOfHousehold, Single)
        i <- 0 to 100000 by 500
        ss <- 0 to 49000 by 500
      } {
        import org.kae.ustax4s.inretirement.MyTaxInRetirement._
        val income = i.tm
        val socialSecurity = ss.tm

        federalTaxDueNoQualifiedInvestments(
          year = year,
          incomeFrom401kEtc = income,
          socSec = socialSecurity
        ) ===
          federalTaxDueUsingForm1040(
            year = year,
            socSec = socialSecurity,
            incomeFrom401k = income,
            qualifiedDividends = 0.tm,
            verbose = false
          )
      }
      success
    }
  }
  "MyTaxInRetirement.taxDueWithInvestments" >> {
    "agrees with MyTaxInRetirement.taxDueUsingForm1040" >> {
      val year = Year.of(2021)
      for {
        status <- List(HeadOfHousehold, Single)
        i <- 0 to 70000 by 1000
        ss <- 0 to 49000 by 1000
        inv <- 0 to 30000 by 1000
      } {
        import org.kae.ustax4s.inretirement.MyTaxInRetirement._
        val income = i.tm
        val socialSecurity = ss.tm
        val qualifiedDividends = inv.tm

        if (federalTaxDue(
          year = year,
          socSec = socialSecurity,
          incomeFrom401kEtc = income,
          qualifiedInvestmentIncome = qualifiedDividends
        ) !=
          federalTaxDueUsingForm1040(
            year = year,
            socSec = socialSecurity,
            incomeFrom401k = income,
            qualifiedDividends = qualifiedDividends,
            verbose = false
          )) {
          println(s"status: $status; i: $i; ss: $ss; inv: $inv")
        }
        federalTaxDue(
          year = year,
          socSec = socialSecurity,
          incomeFrom401kEtc = income,
          qualifiedInvestmentIncome = qualifiedDividends
        ) ===
          federalTaxDueUsingForm1040(
            year = year,
            socSec = socialSecurity,
            incomeFrom401k = income,
            qualifiedDividends = qualifiedDividends,
            verbose = false
          )
      }
      success
    }
  }
}
