package org.kae.ustax4s.calculator

import cats.implicits.*
import java.math.MathContext
import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.federal.{BoundRegime, FederalTaxCalculator, PreTrump}
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable}

class SmokeTestAgainstMyPreTrump2016Return extends FunSuite:
  import org.kae.ustax4s.MoneyConversions.given

  private val regime             = PreTrump
  private val year               = Year.of(2016)
  private val filingStatus       = Kevin.filingStatus(year)
  private val personalExemptions = 2

  private val wages              = Income(153455)
  private val ordinaryDividends  = Income(6932)
  private val qualifiedDividends = Income(5592)

  private val shortTermCapitalLoss = Deduction(3000)
  private val hsaDeduction         = Deduction(0)
  private val itemizedDeductions   = Deduction(31040)

  private val boundRegime = BoundRegime
    .create(
      regime,
      year,
      Kevin.birthDate,
      filingStatus,
      personalExemptions
    )

  test("Form1040 totalTax should match what I filed") {
    val totalIncome         = (wages + ordinaryDividends) applyDeductions shortTermCapitalLoss
    val adjustedGrossIncome = totalIncome applyDeductions hsaDeduction
    val taxableIncome = adjustedGrossIncome applyDeductions
      (itemizedDeductions, boundRegime.personalExemptionDeduction)
    val taxableOrdinaryIncome = taxableIncome reduceBy qualifiedDividends
    val qualifiedIncome       = qualifiedDividends

//    println(s"Total Income:          $totalIncome")
//    println(s"AGI:                   $adjustedGrossIncome")
//    println(s"taxableIncome:         $taxableIncome")
//    println(s"taxableOrdinaryIncome: $taxableOrdinaryIncome")
//    println(s"qualifiedIncome:       $qualifiedIncome")

    val results = boundRegime.calculator
      .federalTaxResults(
        socSec = Income.zero,
        ordinaryIncomeNonSS = adjustedGrossIncome reduceBy qualifiedIncome,
        qualifiedIncome,
        itemizedDeductions
      )
    // println("")
    // println(results.show)

    assert(
      results.personalExemptionDeduction == Deduction(8100)
    )
    assert(
      results.netDeduction == Deduction(39140)
    )
    assert(
      results.taxOnQualifiedIncome.rounded == TaxPayable(839)
    )

    // Note: My tax return used the tax tables, because the taxable amount was
    // < $100k.This introduces some imprecision. So allow for a few dollars
    // difference here.
    assert {
      results.taxOnOrdinaryIncome.isCloseTo(TaxPayable(22464), 3)
    }
    assert {
      results.taxDue.isCloseTo(TaxPayable(23303), 3)
    }
  }
