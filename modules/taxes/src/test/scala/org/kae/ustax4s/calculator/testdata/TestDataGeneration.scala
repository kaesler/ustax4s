package org.kae.ustax4s.calculator.testdata

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.{Regime, Trump}
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*
import org.scalacheck.Gen

// Create test data for the other implementations(TypeScript, Haskell, Purescript)
object TestDataGeneration:

  private val count = 2000

  // For now held constant.
  // TODO: vary year (2021-2055) and regime (Trump NonTrump)
  private val TheRegime             = Trump
  private val TheYear               = Year.of(2021)
  private val TheBirthDate          = LocalDate.of(1955, 10, 2)
  private val TheItemizedDeductions = Money.zero

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
  ) {
    def personalExemptions: Int = dependents + 1
  }

  private val genTestCase: Gen[TestCaseInputs] =
    for
      fs                  <- Gen.oneOf(FilingStatus.values.toSeq)
      dependents          <- Gen.oneOf(0, 1)
      ss                  <- Gen.chooseNum(0, 50000)
      ordinaryIncomeNonSS <- Gen.chooseNum(0, 50000)
      qualifiedIncome     <- Gen.chooseNum(0, 50000)
    yield TestCaseInputs(
      regime = TheRegime,
      year = TheYear,
      birthDate = TheBirthDate,
      filingStatus = fs,
      dependents = dependents,
      socSec = ss.asMoney,
      ordinaryIncomeNonSS = ordinaryIncomeNonSS.asMoney,
      qualifiedIncome = qualifiedIncome.asMoney,
      itemizedDeductions = TheItemizedDeductions
    )

  def testCases: List[TestCaseInputs] = Gen
    .listOfN(count, genTestCase)
    .sample
    .get
