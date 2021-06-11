package org.kae.ustax4s.adhoctests

import java.time.Year
import org.kae.ustax4s.money.TMoney
import org.kae.ustax4s.money.MoneySyntax.*
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.federal.TaxableSocialSecurity
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.kevin.Kevin

object TaxInRetirementTests extends App:
  val year = Year.of(2021)
  val ss   = 49128.asMoney

  doCase(TMoney.zero)
  doCase(17000.asMoney)
  doCase(20000.asMoney)
  doCase(30000.asMoney)
  doCase(40000.asMoney)

  def doCase(income: TMoney): Unit =
    val ssTaxable =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = ss,
        ssRelevantOtherIncome = income
      )
    val totalTaxable = income + ssTaxable
    val tax =
      TaxInRetirement.federalTaxDueUsingForm1040(
        year = year,
        birthDate = Kevin.birthDate,
        filingStatus = Single,
        socSec = ss,
        ordinaryIncomeNonSS = income,
        qualifiedDividends = 0.asMoney,
        verbose = false
      )
    println(
      s"Income: $income, ssTaxable: $ssTaxable, totalTaxable = $totalTaxable, tax: $tax"
    )
