package org.kae.ustax4s.apps

import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.{IntMoneySyntax, TMoney, SimpleTaxInRetirement, TaxableSocialSecurity}
import org.kae.ustax4s._

object TaxInRetirementTests extends App with IntMoneySyntax {
  val ss = 49128.tm
  doCase(TMoney.zero)
  doCase(17000.tm)
  doCase(20000.tm)
  doCase(30000.tm)
  doCase(40000.tm)

  def doCase(income: TMoney) = {
    // TODO: answers seem too low
    val ssTaxable = TaxableSocialSecurity.taxableSocialSecurityBenefits(income, ss)
    val totalTaxable = income + ssTaxable
    val tax = SimpleTaxInRetirement.taxDueWithSS(Single, income, ss)
    println(s"Income: $income, ssTaxable: $ssTaxable, totalTaxable = $totalTaxable, tax: $tax")
  }
}
