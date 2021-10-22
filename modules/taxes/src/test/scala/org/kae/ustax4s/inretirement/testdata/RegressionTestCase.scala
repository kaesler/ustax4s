package org.kae.ustax4s.inretirement.testdata

import cats.Show
import cats.implicits.*
import java.time.Year
import munit.Assertions.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.Trump
import org.kae.ustax4s.inretirement.TaxCalculator
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.Money
import scala.io.Source

final case class RegressionTestCase(
  filingStatus: FilingStatus,
  dependents: Int,
  socSec: Money,
  ordinaryIncomeNonSS: Money,
  qualifiedIncome: Money,
  federalTaxDue: Money,
  stateTaxDue: Money,
  personalExemptions: Int = 0,
  itemizedDeductions: Money = 0
):
  def massachusettsGrossIncome = ordinaryIncomeNonSS + qualifiedIncome

  def run: Unit =
    assertEquals(
      TaxCalculator.federalTaxDue(
        regime = Trump,
        Year.of(2021),
        Kevin.birthDate,
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
        Year.of(2021),
        Kevin.birthDate,
        filingStatus,
        dependents,
        massachusettsGrossIncome
      ),
      stateTaxDue,
      this.toString
    )
end RegressionTestCase

object RegressionTestCase:

  def parseFromCsvLine(s: String): RegressionTestCase =
    val fields              = s.split(',')
    val filingStatus        = FilingStatus.valueOf(fields(0))
    val dependents          = Integer.parseInt(fields(1))
    val socSec              = Money.unsafeParse(fields(2))
    val ordinaryIncomeNonSS = Money.unsafeParse(fields(3))
    val qualifiedIncome     = Money.unsafeParse(fields(4))
    val federalTaxDue       = Money.unsafeParse(fields(5))
    val stateTaxDue         = Money.unsafeParse(fields(6))
    RegressionTestCase(
      filingStatus,
      dependents,
      socSec,
      ordinaryIncomeNonSS,
      qualifiedIncome,
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
