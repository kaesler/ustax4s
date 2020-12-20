package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.{IntMoneySyntax, SimpleTaxInRetirement, TMoney, TaxableSocialSecurity, _}

object TaxInRetirementTests extends App with IntMoneySyntax {
  val year = Year.of(2021)
  val ss = 49128.tm

  doCase(TMoney.zero)
  doCase(17000.tm)
  doCase(20000.tm)
  doCase(30000.tm)
  doCase(40000.tm)

  def doCase(income: TMoney): Unit = {
    val ssTaxable =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(income, ss)
    val totalTaxable = income + ssTaxable
    val tax =
      SimpleTaxInRetirement.taxDueUsingForm1040(year, Single, income, ss)
    println(
      s"Income: $income, ssTaxable: $ssTaxable, totalTaxable = $totalTaxable, tax: $tax"
    )
  }
}
