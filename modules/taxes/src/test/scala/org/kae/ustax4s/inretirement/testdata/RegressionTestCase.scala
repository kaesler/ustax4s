package org.kae.ustax4s.inretirement.testdata

import cats.Show
import cats.implicits.*
import java.time.Year
import munit.Assertions.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.money.*
import org.kae.ustax4s.money.given
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.kevin.Kevin
import scala.io.Source

final case class RegressionTestCase(
  filingStatus: FilingStatus,
  dependents: Int,
  socSec: TMoney,
  ordinaryIncomeNonSS: TMoney,
  qualifiedIncome: TMoney,
  federalTaxDue: TMoney,
  stateTaxDue: TMoney
):
  def massachusettsGrossIncome = ordinaryIncomeNonSS + qualifiedIncome

  def run: Unit =
    assertEquals(
      TaxInRetirement.federalTaxDue(
        Year.of(2021),
        Kevin.birthDate,
        filingStatus,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome
      ),
      federalTaxDue,
      this.toString
    )
    assertEquals(
      TaxInRetirement.stateTaxDue(
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
    val socSec              = TMoney.unsafeParse(fields(2))
    val ordinaryIncomeNonSS = TMoney.unsafeParse(fields(3))
    val qualifiedIncome     = TMoney.unsafeParse(fields(4))
    val federalTaxDue       = TMoney.unsafeParse(fields(5))
    val stateTaxDue         = TMoney.unsafeParse(fields(6))
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
