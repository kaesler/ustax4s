package org.kae.ustax4s.inretirement

import cats.implicits.*
import java.math.MathContext
import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.federal.{FederalTaxCalculator, NonTrump}
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.*
import org.kae.ustax4s.money.MoneySyntax.*

class SmokeTestAgainstMyPreTrump2016Return extends FunSuite:
  private val regime             = NonTrump
  private val year               = Year.of(2016)
  private val filingStatus       = Kevin.filingStatus(year)
  private val personalExemptions = 2

  private val wages              = 153455.asMoney
  private val ordinaryDividends  = 6932.asMoney
  private val qualifiedDividends = 5592.asMoney

  private val shortTermCapitalLoss = 3000.asMoney
  private val hsaDeduction         = 0.asMoney
  private val itemizedDeductions   = 31040.asMoney

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

    val results = FederalTaxCalculator
      .create(
        regime,
        year,
        Kevin.birthDate,
        filingStatus,
        personalExemptions
      )
      .federalTaxResults(
        socSec = 0,
        ordinaryIncomeNonSS = adjustedGrossIncome subp qualifiedIncome,
        qualifiedIncome,
        itemizedDeductions
      )
    println("")
    println(results.show)

    assert(
      results.personalExceptionDeduction == 8100.asMoney
    )
    assert(
      results.netDeduction == 39140.asMoney
    )
    assert(
      results.taxOnQualifiedIncome.rounded == 839.asMoney
    )

    // Note: My tax return used the tax tables, because the taxable amount was
    // < $100k.This introduces some imprecision. So allow for a few dollars
    // difference here.
    assert {
      results.taxOnOrdinaryIncome.isCloseTo(22464.asMoney, 3)
    }
    assert {
      results.taxDue.isCloseTo(23303.asMoney, 3)
    }
  }
