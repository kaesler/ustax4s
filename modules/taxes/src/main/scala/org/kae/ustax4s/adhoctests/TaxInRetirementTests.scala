package org.kae.ustax4s.adhoctests

import java.time.Year
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.federal.TaxableSocialSecurity
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.kevin.Kevin

object TaxInRetirementTests extends App:
  val year = Year.of(2021)
  val ss   = 49128.asMoney

  doCase(0)
  doCase(17000)
  doCase(20000)
  doCase(30000)
  doCase(40000)

  def doCase(income: Money): Unit =
    val ssTaxable =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = ss,
        ssRelevantOtherIncome = income
      )
    val totalTaxable = income + ssTaxable
    val tax =
      TaxCalculator.federalTaxDueUsingForm1040(
        year = year,
        birthDate = Kevin.birthDate,
        filingStatus = Single,
        socSec = ss,
        ordinaryIncomeNonSS = income,
        qualifiedDividends = 0,
        verbose = false
      )
    println(
      s"Income: $income, ssTaxable: $ssTaxable, totalTaxable = $totalTaxable, tax: $tax"
    )
