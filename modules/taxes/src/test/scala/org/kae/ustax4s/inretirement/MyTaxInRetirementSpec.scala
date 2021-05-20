package org.kae.ustax4s.inretirement

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

class MyTaxInRetirementSpec extends FunSuite {

  test(
    "MyTaxInRetirement.taxDue " +
      "agrees with MyTaxInRetirement.taxDueUsingForm1040, " +
      "with no qualified income"
  ) {
    val year = Year.of(2021)
    for {
      i  <- 0 to 100000 by 500
      ss <- 0 to 49000 by 500
    } {
      import org.kae.ustax4s.inretirement.MyTaxInRetirement._
      val income         = i.tm
      val socialSecurity = ss.tm

      assertEquals(
        federalTaxDue(
          year = year,
          socSec = socialSecurity,
          ordinaryIncomeNonSS = income,
          qualifiedIncome = 0.tm
        ),
        federalTaxDueUsingForm1040(
          year = year,
          socSec = socialSecurity,
          ordinaryIncomeNonSS = income,
          qualifiedDividends = 0.tm,
          verbose = false
        )
      )
    }
  }

  test(
    "MyTaxInRetirement.taxDueWithInvestments " +
      "agrees with MyTaxInRetirement.taxDueUsingForm1040, " +
      "with qualified income"
  ) {
    val year = Year.of(2021)
    for {
      status <- List(HeadOfHousehold, Single)
      i      <- 0 to 70000 by 1000
      ss     <- 0 to 49000 by 1000
      inv    <- 0 to 30000 by 1000
    } {
      import org.kae.ustax4s.inretirement.MyTaxInRetirement._
      val income             = i.tm
      val socialSecurity     = ss.tm
      val qualifiedDividends = inv.tm

      if (
        federalTaxDue(
          year = year,
          socSec = socialSecurity,
          ordinaryIncomeNonSS = income,
          qualifiedIncome = qualifiedDividends
        ) !=
          federalTaxDueUsingForm1040(
            year = year,
            socSec = socialSecurity,
            ordinaryIncomeNonSS = income,
            qualifiedDividends = qualifiedDividends,
            verbose = false
          )
      ) {
        println(s"status: $status; i: $i; ss: $ss; inv: $inv")
      }

      assertEquals(
        federalTaxDue(
          year = year,
          socSec = socialSecurity,
          ordinaryIncomeNonSS = income,
          qualifiedIncome = qualifiedDividends
        ),
        federalTaxDueUsingForm1040(
          year = year,
          socSec = socialSecurity,
          ordinaryIncomeNonSS = income,
          qualifiedDividends = qualifiedDividends,
          verbose = false
        )
      )
    }
  }
}