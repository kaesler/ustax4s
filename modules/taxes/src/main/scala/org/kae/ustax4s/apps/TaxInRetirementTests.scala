package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.{IntMoneySyntax, SimpleTaxInRetirement, TMoney, TaxableSocialSecurity, _}

object TaxInRetirementTests extends App with IntMoneySyntax {
  val year = Year.of(2021)
  val ss   = 49128.tm

  doCase(TMoney.zero)
  doCase(17000.tm)
  doCase(20000.tm)
  doCase(30000.tm)
  doCase(40000.tm)

  def doCase(income: TMoney): Unit = {
    val ssTaxable =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = ss,
        relevantIncome = income
      )
    val totalTaxable = income + ssTaxable
    val tax =
      SimpleTaxInRetirement.taxDueUsingForm1040(
        year = year,
        filingStatus = Single,
        socSec = ss,
        incomeFrom401k = income,
        qualifiedDividends = 0.tm,
        verbose = false
      )
    println(
      s"Income: $income, ssTaxable: $ssTaxable, totalTaxable = $totalTaxable, tax: $tax"
    )
  }
}
