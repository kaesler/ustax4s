package org.kae.ustax4s.adhoctests

import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.federal.TaxableSocialSecurity
import org.kae.ustax4s.money.Money

object MyExpectedSSTax extends App:
  val ssRelevantOtherIncomeFromRmd = Money(17000)
  val socialSecurityBenefits       = Money(49000)

  val taxableSS = TaxableSocialSecurity.taxableSocialSecurityBenefits(
    filingStatus = Single,
    socialSecurityBenefits = socialSecurityBenefits,
    ssRelevantOtherIncome = ssRelevantOtherIncomeFromRmd
  )
  val taxableIncome     = ssRelevantOtherIncomeFromRmd + taxableSS
  val fractionSSTaxable = taxableSS div socialSecurityBenefits
  println(s"Taxable SS = $taxableSS ($fractionSSTaxable)")
  println(s"Taxable income = $taxableIncome")
