package org.kae.ustax4s.calculator.testdata.knownyears

import cats.Show
import cats.implicits.*
import java.time.{LocalDate, Year}
import munit.Assertions.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable, TaxableIncome}
import scala.io.Source

final case class KnownYearRegressionTestCase(
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
    assertEquals(
      TaxCalculator.federalTaxDue(
        year,
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
        TaxCalculator.federalTaxResults(
          year,
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
        year,
        filingStatus,
        birthDate,
        dependents,
        massachusettsGrossIncome
      ),
      stateTaxDue,
      this.toString
    )
end KnownYearRegressionTestCase

object KnownYearRegressionTestCase:

  given show: Show[KnownYearRegressionTestCase] = new Show[KnownYearRegressionTestCase]:
    override def show(tc: KnownYearRegressionTestCase): String =
      import tc.*
      s"""
         |RegressionTestCase(
         |  year                = $year,
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
  end show

  private def parseFromCsvLine(s: String): KnownYearRegressionTestCase =
    val fields               = s.split(',')
    val year: Year           = Year.of(Integer.parseInt(fields(0)))
    val birthDate: LocalDate = LocalDate.parse(fields(1))
    val filingStatus         = FilingStatus.valueOf(fields(2))
    val dependents           = Integer.parseInt(fields(3))
    val socSec               = Income.unsafeParse(fields(4))
    val ordinaryIncomeNonSS  = Income.unsafeParse(fields(5))
    val qualifiedIncome      = TaxableIncome.unsafeParse(fields(6))
    val itemizedDeductions   = Deduction.unsafeParse(fields(7))

    val federalTaxDue = TaxPayable.unsafeParse(fields(8))
    val stateTaxDue   = TaxPayable.unsafeParse(fields(9))

    KnownYearRegressionTestCase(
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

  def all: List[KnownYearRegressionTestCase] =
    Source
      .fromResource("knownYearRegressionTestCases.csv")
      .getLines()
      .drop(1)
      .toList
      .map(parseFromCsvLine)
end KnownYearRegressionTestCase
