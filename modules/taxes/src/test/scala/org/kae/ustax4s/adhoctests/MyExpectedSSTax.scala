package org.kae.ustax4s.adhoctests

import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.federal.TaxableSocialSecurity
import org.kae.ustax4s.money.Income

object MyExpectedSSTax extends App:
  val ssRelevantOtherIncomeFromRmd = Income(17000)
  val socialSecurityBenefits       = Income(49000)

  val taxableSS = TaxableSocialSecurity.taxableSocialSecurityBenefits(
    filingStatus = Single,
    socialSecurityBenefits = socialSecurityBenefits,
    ssRelevantOtherIncome = ssRelevantOtherIncomeFromRmd
  )
  val taxableIncome     = ssRelevantOtherIncomeFromRmd + taxableSS
  val fractionSSTaxable = taxableSS div socialSecurityBenefits
  println(s"Taxable SS = $taxableSS ($fractionSSTaxable)")
  println(s"Taxable income = $taxableIncome")
