package org.kae.ustax4s.calculator.testdata

import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.money.MoneySyntax.*
import org.kae.ustax4s.money.Money
import org.scalacheck.Gen

// Create test data for the other implementations(TypeScript, Haskell, Purescript)
object TestDataGeneration:

  private val count = 2000

  final case class TestCaseInputs(
    filingStatus: FilingStatus,
    dependents: Int,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money
  )

  private val genTestCase: Gen[TestCaseInputs] =
    for
      fs                  <- Gen.oneOf(FilingStatus.values.toSeq)
      dependents          <- Gen.oneOf(0, 1)
      ss                  <- Gen.chooseNum(0, 50000)
      ordinaryIncomeNonSS <- Gen.chooseNum(0, 50000)
      qualifiedIncome     <- Gen.chooseNum(0, 50000)
    yield TestCaseInputs(
      filingStatus = fs,
      dependents = dependents,
      socSec = ss.asMoney,
      ordinaryIncomeNonSS = ordinaryIncomeNonSS.asMoney,
      qualifiedIncome = qualifiedIncome.asMoney
    )

  def testCases: List[TestCaseInputs] = Gen
    .listOfN(count, genTestCase)
    .sample
    .get