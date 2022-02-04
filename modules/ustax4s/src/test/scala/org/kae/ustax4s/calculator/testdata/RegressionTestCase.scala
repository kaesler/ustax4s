package org.kae.ustax4s.calculator.testdata

import cats.Show
import cats.implicits.*
import java.time.{LocalDate, Year}
import munit.Assertions.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.federal.{BoundRegime, Regime, Trump}
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome, TaxPayable}
import scala.io.Source

final case class RegressionTestCase(
  regime: Regime,
  year: Year,
  birthDate: LocalDate,
  filingStatus: FilingStatus,
  dependents: Int,
  socSec: Income,
  ordinaryIncomeNonSS: Income,
  qualifiedIncome: TaxableIncome,
  itemizedDeductions: Deduction,
  federalTaxDue: TaxPayable,
  stateTaxDue: TaxPayable
):
  def personalExemptions: Int          = dependents + 1
  def massachusettsGrossIncome: Income = ordinaryIncomeNonSS + qualifiedIncome

  def run(): Unit =
    if federalTaxDue != TaxCalculator.federalTaxDue(
        year,
        birthDate,
        filingStatus,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
    then {
      println(this)
      val results = BoundRegime
        .createForKnownYear(
          year,
          birthDate,
          filingStatus,
          personalExemptions
        )
        .calculator
        .federalTaxResults(
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )
      println(results.show)
    }
    assertEquals(
      TaxCalculator.federalTaxDue(
        // regime,
        year,
        birthDate,
        filingStatus,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      ),
      federalTaxDue,
      this.toString
    )
    assertEquals(
      TaxCalculator.stateTaxDue(
        year,
        birthDate,
        filingStatus,
        dependents,
        massachusettsGrossIncome
      ),
      stateTaxDue,
      this.toString
    )
end RegressionTestCase

object RegressionTestCase:

  private def parseFromCsvLine(s: String): RegressionTestCase =
    val fields               = s.split(',')
    val regime: Regime       = Regime.unsafeParse(fields(0))
    val year: Year           = Year.of(Integer.parseInt(fields(1)))
    val birthDate: LocalDate = LocalDate.parse(fields(2))
    val filingStatus         = FilingStatus.valueOf(fields(3))
    val dependents           = Integer.parseInt(fields(4))
    val socSec               = Income.unsafeParse(fields(5))
    val ordinaryIncomeNonSS  = Income.unsafeParse(fields(6))
    val qualifiedIncome      = TaxableIncome.unsafeParse(fields(7))
    val itemizedDeductions   = Deduction.unsafeParse(fields(8))

    val federalTaxDue = TaxPayable.unsafeParse(fields(9))
    val stateTaxDue   = TaxPayable.unsafeParse(fields(10))

    RegressionTestCase(
      regime,
      year,
      birthDate,
      filingStatus,
      dependents,
      socSec,
      ordinaryIncomeNonSS,
      qualifiedIncome,
      itemizedDeductions,
      federalTaxDue,
      stateTaxDue
    )

  def all: List[RegressionTestCase] =
    Source
      .fromResource("regressionTestCases.csv")
      .getLines()
      .drop(1)
      .toList
      .map(parseFromCsvLine)
end RegressionTestCase
