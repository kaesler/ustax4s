package org.kae.ustax4s
package apps

object MyExpectedSSTax extends App with IntMoneySyntax {
  val relevantIncomeFromRmd = 20000.tm
  val socialSecurityBenefits = 49128.tm

  val taxableSS = TaxableSocialSecurity.taxableSocialSecurityBenefits(relevantIncomeFromRmd, socialSecurityBenefits)
  val taxableIncome = relevantIncomeFromRmd + taxableSS
  println(s"Taxable SS = $taxableSS")
  println(s"Taxable income = $taxableIncome")

}
