package org.kae.ustax4s.calculator

import cats.implicits.*
import java.math.MathContext
import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.federal.{BoundRegime, FederalTaxCalculator, PreTrump}
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.*

class SmokeTestAgainstMyPreTrump2017Return extends FunSuite:
  private val regime             = PreTrump
  private val year               = Year.of(2017)
  private val filingStatus       = Kevin.filingStatus(year)
  private val personalExemptions = 2

  private val wages              = Money(128270)
  private val ordinaryDividends  = Money(9196)
  private val qualifiedDividends = Money(7686)

  private val shortTermCapitalLoss = Money(2419)
  private val hsaDeduction         = Money(750)
  private val itemizedDeductions   = Money(22529)

  private val boundRegime = BoundRegime
    .create(
      regime,
      year,
      Kevin.birthDate,
      filingStatus,
      personalExemptions
    )

  test("Form1040 totalTax should match what I filed") {
    val totalIncome         = wages + ordinaryDividends subp shortTermCapitalLoss
    val adjustedGrossIncome = totalIncome subp hsaDeduction
    val taxableIncome =
      adjustedGrossIncome subp itemizedDeductions subp boundRegime.personalExemptionDeduction
    val taxableOrdinaryIncome = taxableIncome subp qualifiedDividends
    val qualifiedIncome       = qualifiedDividends

//    println(s"Total Income:          $totalIncome")
//    println(s"AGI:                   $adjustedGrossIncome")
//    println(s"taxableIncome:         $taxableIncome")
//    println(s"taxableOrdinaryIncome: $taxableOrdinaryIncome")
//    println(s"qualifiedIncome:       $qualifiedIncome")

    val results = boundRegime.calculator
      .federalTaxResults(
        socSec = 0,
        ordinaryIncomeNonSS = adjustedGrossIncome subp qualifiedIncome,
        qualifiedIncome,
        itemizedDeductions
      )
    // println("")
    // println(results.show)

    assert(
      results.personalExemptionDeduction == Money(8100)
    )
    assert(
      results.netDeduction == Money(30629)
    )
    assert(
      results.taxOnQualifiedIncome.rounded == Money(1153)
    )

    // Note: My tax return used the tax tables, because the taxable amount was
    // < $100k.This introduces some imprecision. So allow for a few dollars
    // difference here.
    assert {
      results.taxOnOrdinaryIncome.isCloseTo(Money(18246), 2)
    }
    assert {
      results.taxDue.isCloseTo(Money(19399), 2)
    }
  }
