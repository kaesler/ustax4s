package org.kae.ustax4s.calculator.testdata.futureyears

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.Regime
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome}
import org.scalacheck.Gen

// Create test data for the other implementations(TypeScript, Haskell, Purescript)
object TestDataGeneration:
  import org.kae.ustax4s.money.MoneyConversions.given

  private val count = 6000

  // For now held constant.
  private val TheBirthDate = LocalDate.of(1955, 10, 2)

  final case class TestCaseInputs(
    regime: Regime,
    futureYear: Year,
    estimatedAnnualInflationFactor: Double,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    dependents: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  ):
    require(futureYear.getValue > YearlyValues.last.year.getValue)
    require(estimatedAnnualInflationFactor > 1.0)
    def personalExemptions: Int = dependents + 1
  end TestCaseInputs

  private val genTestCase: Gen[TestCaseInputs] =
    val earliestYear = YearlyValues.last.year.getValue + 1
    for
      regime              <- Gen.oneOf(Regime.values.toList)
      yearNum             <- Gen.chooseNum(earliestYear, earliestYear + 30)
      estimate            <- Gen.chooseNum(1.01, 1.10)
      fs                  <- Gen.oneOf(FilingStatus.values.toSeq)
      dependents          <- Gen.oneOf(0, 4)
      ss                  <- Gen.chooseNum(0, 50000)
      ordinaryIncomeNonSS <- Gen.chooseNum(0, 150000)
      qualifiedIncome     <- Gen.chooseNum(0, 100000)
      itemizedDeductions  <- Gen.chooseNum(0, 30000)
    yield TestCaseInputs(
      regime = regime,
      futureYear = Year.of(yearNum),
      estimatedAnnualInflationFactor = estimate,
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
