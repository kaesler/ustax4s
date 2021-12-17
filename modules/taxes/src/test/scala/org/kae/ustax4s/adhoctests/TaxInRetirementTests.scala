package org.kae.ustax4s.adhoctests

import java.time.Year
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.federal.TaxableSocialSecurity
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.Income

object TaxInRetirementTests extends App:
  import org.kae.ustax4s.MoneyConversions.given

  val year = Year.of(2021)
  val ss   = 49128

  doCase(0)
  doCase(17000)
  doCase(20000)
  doCase(30000)
  doCase(40000)

  def doCase(income: Income): Unit =
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
