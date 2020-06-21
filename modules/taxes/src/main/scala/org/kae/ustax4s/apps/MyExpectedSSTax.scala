package org.kae.ustax4s
package apps

import org.kae.ustax4s.{IntMoneySyntax, TaxableSocialSecurity}


object MyExpectedSSTax extends App with IntMoneySyntax {
  val relevantIncomeFromRmd = 17500.tm
  val socialSecurityBenefits = 49128.tm

  val taxableSS = TaxableSocialSecurity.taxableSocialSecurityBenefits(relevantIncomeFromRmd, socialSecurityBenefits)
  val taxableIncome = relevantIncomeFromRmd + taxableSS
  println(s"Taxable SS = $taxableSS")
  println(s"Taxable income = $taxableIncome")

}
