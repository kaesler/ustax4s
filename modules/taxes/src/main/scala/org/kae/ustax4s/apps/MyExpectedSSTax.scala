package org.kae.ustax4s

package apps

import org.kae.ustax4s.FilingStatus.Single

object MyExpectedSSTax extends App with IntMoneySyntax {
  val relevantIncomeFromRmd = 17000.tm
  val socialSecurityBenefits = 49000.tm

  val taxableSS = TaxableSocialSecurity.taxableSocialSecurityBenefits(
    Single,
    relevantIncomeFromRmd,
    socialSecurityBenefits
  )
  val taxableIncome = relevantIncomeFromRmd + taxableSS
  val fractionSSTaxable = taxableSS div socialSecurityBenefits
  println(s"Taxable SS = $taxableSS ($fractionSSTaxable)")
  println(s"Taxable income = $taxableIncome")

}
