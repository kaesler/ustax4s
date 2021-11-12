package org.kae.ustax4s.calculator.testdata

import cats.Show
import cats.implicits.*
import java.time.{LocalDate, Year}
import munit.Assertions.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.federal.{Regime, Trump}
import org.kae.ustax4s.money.Money
import scala.io.Source

final case class RegressionTestCase(
  regime: Regime,
  year: Year,
  birthDate: LocalDate,
  filingStatus: FilingStatus,
  dependents: Int,
  socSec: Money,
  ordinaryIncomeNonSS: Money,
  qualifiedIncome: Money,
  itemizedDeductions: Money,
  federalTaxDue: Money,
  stateTaxDue: Money
):
  def personalExemptions: Int         = dependents + 1
  def massachusettsGrossIncome: Money = ordinaryIncomeNonSS + qualifiedIncome

  def run(): Unit =
    assertEquals(
      TaxCalculator.federalTaxDue(
        regime,
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
    val fields                    = s.split(',')
    val regime: Regime            = Regime.unsafeParse(fields(0))
    val year: Year                = Year.of(Integer.parseInt(fields(1)))
    val birthDate: LocalDate      = LocalDate.parse(fields(2))
    val filingStatus              = FilingStatus.valueOf(fields(3))
    val dependents                = Integer.parseInt(fields(4))
    val socSec                    = Money.unsafeParse(fields(5))
    val ordinaryIncomeNonSS       = Money.unsafeParse(fields(6))
    val qualifiedIncome           = Money.unsafeParse(fields(7))
    val itemizedDeductions: Money = Money.unsafeParse(fields(8))

    val federalTaxDue = Money.unsafeParse(fields(9))
    val stateTaxDue   = Money.unsafeParse(fields(10))

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
