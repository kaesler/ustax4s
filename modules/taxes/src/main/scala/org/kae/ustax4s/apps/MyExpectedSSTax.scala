package org.kae.ustax4s
package apps

import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.federal.TaxableSocialSecurity

object MyExpectedSSTax extends App with IntMoneySyntax:
  val ssRelevantOtherIncomeFromRmd = 17000.asMoney
  val socialSecurityBenefits       = 49000.asMoney

  val taxableSS = TaxableSocialSecurity.taxableSocialSecurityBenefits(
    filingStatus = Single,
    socialSecurityBenefits = socialSecurityBenefits,
    ssRelevantOtherIncome = ssRelevantOtherIncomeFromRmd
  )
  val taxableIncome     = ssRelevantOtherIncomeFromRmd + taxableSS
  val fractionSSTaxable = taxableSS div socialSecurityBenefits
  println(s"Taxable SS = $taxableSS ($fractionSSTaxable)")
  println(s"Taxable income = $taxableIncome")
