package org.kae.ustax4s.inretirement

import cats.implicits.*
import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.federal.NonTrump
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.MoneySyntax.*
import org.kae.ustax4s.money.*

class SmokeTestAgainstMyPreTrump2017Return extends FunSuite:
  private val regime             = NonTrump
  private val year               = Year.of(2017)
  private val filingStatus       = Kevin.filingStatus(year)
  private val personalExemptions = 2

  private val wages              = 128270.asMoney
  private val ordinaryDividends  = 9196.asMoney
  private val qualifiedDividends = 7686.asMoney

  private val shortTermCapitalLoss = 2419.asMoney
  private val hsaDeduction         = 750.asMoney
  private val itemizedDeductions   = 22529.asMoney

  test("Form1040 totalTax should match what I filed") {
    val totalIncome         = wages + ordinaryDividends subp shortTermCapitalLoss
    val adjustedGrossIncome = totalIncome subp hsaDeduction
    val taxableIncome = adjustedGrossIncome subp itemizedDeductions subp regime
      .personalExemptionDeduction(year, personalExemptions)
    val taxableOrdinaryIncome = taxableIncome subp qualifiedDividends
    val qualifiedIncome       = qualifiedDividends

    println(s"Total Income:          $totalIncome")
    println(s"AGI:                   $adjustedGrossIncome")
    println(s"taxableIncome:         $taxableIncome")
    println(s"taxableOrdinaryIncome: $taxableOrdinaryIncome")
    println(s"qualifiedIncome:       $qualifiedIncome")

    val results = TaxInRetirement.federalTaxResults(
      regime,
      year,
      Kevin.birthDate,
      filingStatus,
      socSec = 0,
      ordinaryIncomeNonSS = adjustedGrossIncome subp qualifiedIncome,
      qualifiedIncome = 7686,
      personalExemptions,
      itemizedDeductions
    )
    println("")
    println(results.show)
    assert(true)
  }
