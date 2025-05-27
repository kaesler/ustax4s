package org.kae.ustax4s.calculator.testdata.futureyears

import cats.Show
import cats.implicits.*
import java.time.{LocalDate, Year}
import munit.Assertions.*
import org.kae.ustax4s.{FilingStatus, SourceLoc as SourceLoc}
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.federal.Regime
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable, TaxableIncome}
import scala.io.Source

final case class FutureYearRegressionTestCase(
  regime: Regime,
  futureYear: Year,
  estimatedAnnualInflationFactor: Double,
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
  require(futureYear.getValue > YearlyValues.last.year.getValue, SourceLoc.loc)

  def personalExemptions: Int          = dependents + 1
  def massachusettsGrossIncome: Income = ordinaryIncomeNonSS + qualifiedIncome

  def run(): Unit =
    assertEquals(
      TaxCalculator.federalTaxDueForFutureYear(
        regime,
        futureYear,
        estimatedAnnualInflationFactor,
        filingStatus,
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      ),
      federalTaxDue,
      this.show ++
        "\n" ++
        TaxCalculator.federalTaxResultsForFutureYear(
          regime,
          futureYear,
          estimatedAnnualInflationFactor,
          filingStatus,
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )
          .show
    )
    assertEquals(
      TaxCalculator.stateTaxDue(
        futureYear,
        filingStatus,
        birthDate,
        dependents,
        massachusettsGrossIncome
      ),
      stateTaxDue,
      this.toString
    )
end FutureYearRegressionTestCase

object FutureYearRegressionTestCase:

  given show: Show[FutureYearRegressionTestCase] = new Show[FutureYearRegressionTestCase] {
    override def show(tc: FutureYearRegressionTestCase): String =
      import tc.*
      s"""
         |FutureYearRegressionTestCase(
         |  regime              = $regime,
         |  year                = $futureYear,
         |  estimatedInflation  = $estimatedAnnualInflationFactor,
         |  birthDate           = $birthDate,
         |  filingStatus        = ${filingStatus.show},
         |  dependents          = $dependents,
         |  socSec              = $socSec,
         |  ordinaryIncomeNonSS = $ordinaryIncomeNonSS,
         |  qualifiedIncome     = $qualifiedIncome,
         |  itemizedDeductions  = $itemizedDeductions,
         |  federalTaxDue       = $federalTaxDue,
         |  stateTaxDue         = $stateTaxDue
         |}
         |""".stripMargin
  }

  private def parseFromCsvLine(s: String): FutureYearRegressionTestCase =
    val fields               = s.split(',')
    val regime               = Regime.parse(fields(0)).get
    val year: Year           = Year.of(Integer.parseInt(fields(1)))
    val estimate: Double     = java.lang.Double.parseDouble(fields(2))
    val birthDate: LocalDate = LocalDate.parse(fields(3))
    val filingStatus         = FilingStatus.valueOf(fields(4))
    val dependents           = Integer.parseInt(fields(5))
    val socSec               = Income.unsafeParse(fields(6))
    val ordinaryIncomeNonSS  = Income.unsafeParse(fields(7))
    val qualifiedIncome      = TaxableIncome.unsafeParse(fields(8))
    val itemizedDeductions   = Deduction.unsafeParse(fields(9))

    val federalTaxDue = TaxPayable.unsafeParse(fields(10))
    val stateTaxDue   = TaxPayable.unsafeParse(fields(11))

    FutureYearRegressionTestCase(
      regime,
      year,
      estimate,
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

  def all: List[FutureYearRegressionTestCase] =
    Source
      .fromResource("futureYearRegressionTestCases.csv")
      .getLines()
      .drop(1)
      .toList
      .map(parseFromCsvLine)
end FutureYearRegressionTestCase
