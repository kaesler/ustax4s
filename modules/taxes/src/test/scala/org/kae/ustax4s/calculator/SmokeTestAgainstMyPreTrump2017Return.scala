package org.kae.ustax4s.calculator

import cats.implicits.*
import java.math.MathContext
import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.federal.{BoundRegime, FederalTaxCalculator, PreTrump}
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome, TaxPayable}

class SmokeTestAgainstMyPreTrump2017Return extends FunSuite:
  private val regime             = PreTrump
  private val year               = Year.of(2017)
  private val filingStatus       = Kevin.filingStatus(year)
  private val personalExemptions = 2

  private val wages              = Income(128270)
  private val ordinaryDividends  = Income(9196)
  private val qualifiedDividends = TaxableIncome(7686)

  private val shortTermCapitalLoss = Deduction(2419)
  private val hsaDeduction         = Deduction(750)
  private val itemizedDeductions   = Deduction(22529)

  private val boundRegime = BoundRegime
    .create(
      regime,
      year,
      Kevin.birthDate,
      filingStatus,
      personalExemptions
    )

  test("Form1040 totalTax should match what I filed") {
    val totalIncome: Income = (wages + ordinaryDividends) applyAdjustments shortTermCapitalLoss
    val adjustedGrossIncome: Income = totalIncome applyAdjustments hsaDeduction
    val taxableIncome =
      adjustedGrossIncome applyDeductions
        (itemizedDeductions, boundRegime.personalExemptionDeduction)
    // val taxableOrdinaryIncome = taxableIncome reduceBy qualifiedDividends
    // val qualifiedIncome       = qualifiedDividends

//    println(s"Total Income:          $totalIncome")
//    println(s"AGI:                   $adjustedGrossIncome")
//    println(s"taxableIncome:         $taxableIncome")
//    println(s"taxableOrdinaryIncome: $taxableOrdinaryIncome")
//    println(s"qualifiedIncome:       $qualifiedIncome")

    val results = boundRegime.calculator
      .federalTaxResults(
        socSec = Income.zero,
        ordinaryIncomeNonSS = adjustedGrossIncome reduceBy qualifiedDividends,
        qualifiedIncome = qualifiedDividends,
        itemizedDeductions
      )
    // println("")
    // println(results.show)

    assert(
      results.personalExemptionDeduction == Deduction(8100)
    )
    assert(
      results.netDeduction == Deduction(30629)
    )
    assert(
      results.taxOnQualifiedIncome.rounded == TaxPayable(1153)
    )

    // Note: My tax return used the tax tables, because the taxable amount was
    // < $100k.This introduces some imprecision. So allow for a few dollars
    // difference here.
    assert {
      results.taxOnOrdinaryIncome.isCloseTo(TaxPayable(18246), 2)
    }
    assert {
      results.taxDue.isCloseTo(TaxPayable(19399), 2)
    }
  }
