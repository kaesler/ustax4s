package org.kae.ustax4s.calculator.testdata

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.{NonTrump, Regime, Trump}
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*
import org.scalacheck.Gen

// Create test data for the other implementations(TypeScript, Haskell, Purescript)
object TestDataGeneration:

  private val count = 4000

  // For now held constant.
  private val TheBirthDate = LocalDate.of(1955, 10, 2)

  final case class TestCaseInputs(
    regime: Regime,
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    dependents: Int,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    itemizedDeductions: Money
  ):
    def personalExemptions: Int = dependents + 1
  end TestCaseInputs

  private val genTestCase: Gen[TestCaseInputs] =
    for
      regime <- Gen.oneOf(NonTrump, Trump)
      yearNum <-
        if regime == NonTrump then Gen.const(2017)
        else Gen.chooseNum(2018, 2022)
      fs                  <- Gen.oneOf(FilingStatus.values.toSeq)
      dependents          <- Gen.oneOf(0, 4)
      ss                  <- Gen.chooseNum(0, 50000)
      ordinaryIncomeNonSS <- Gen.chooseNum(0, 50000)
      qualifiedIncome     <- Gen.chooseNum(0, 50000)
      itemizedDeductions  <- Gen.chooseNum(0, 15000)
    yield TestCaseInputs(
      regime = regime,
      year = Year.of(yearNum),
      birthDate = TheBirthDate,
      filingStatus = fs,
      dependents = dependents,
      socSec = ss.asMoney,
      ordinaryIncomeNonSS = ordinaryIncomeNonSS,
      qualifiedIncome = qualifiedIncome,
      itemizedDeductions = itemizedDeductions
    )

  def testCases: List[TestCaseInputs] = Gen
    .listOfN(count, genTestCase)
    .sample
    .get
