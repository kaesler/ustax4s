package org.kae.ustax4s.federal.testdata.knownyears

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome}
import org.scalacheck.Gen
import scala.language.implicitConversions

// Create test data for the other implementations(TypeScript, Haskell, Purescript)
object TestDataGeneration:
  import org.kae.ustax4s.money.MoneyConversions.given

  private val count = 6000

  // For now held constant.
  private val TheBirthDate = LocalDate.of(1955, 10, 2)

  final case class TestCaseInputs(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    dependents: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  ):
    def personalExemptions: Int = dependents + 1
  end TestCaseInputs

  private val genTestCase: Gen[TestCaseInputs] =
    val earliestYear = YearlyValues.first.year.getValue
    val latestYear = YearlyValues.last.year.getValue
    for
      yearNum             <- Gen.chooseNum(earliestYear, latestYear)
      fs                  <- Gen.oneOf(FilingStatus.values.toSeq)
      dependents          <- Gen.oneOf(0, 4)
      ss                  <- Gen.chooseNum(0, 50000)
      ordinaryIncomeNonSS <- Gen.chooseNum(0, 150000)
      qualifiedIncome     <- Gen.chooseNum(0, 100000)
      itemizedDeductions  <- Gen.chooseNum(0, 30000)
    yield TestCaseInputs(
      year = Year.of(yearNum),
      birthDate = TheBirthDate,
      filingStatus = fs,
      dependents = dependents,
      socSec = ss,
      ordinaryIncomeNonSS = ordinaryIncomeNonSS,
      qualifiedIncome = qualifiedIncome,
      itemizedDeductions = itemizedDeductions
    )

  def testCases: List[TestCaseInputs] = Gen
    .listOfN(count, genTestCase)
    .sample
    .get
