package org.kae.ustax4s.apps

import org.kae.ustax4s.{FilingStatus, IntMoneySyntax, TMoney}
import org.scalacheck.Gen

// Create test data for the other implementations(TypeScript, Haskell, Purescript)
object TestDataGeneration extends IntMoneySyntax {

  private val count = 2000

  final case class TestCase(
    filingStatus: FilingStatus,
    qualifiedIncome: TMoney,
    ordinaryIncomeNonSS: TMoney,
    socSec: TMoney
  )

  private val genTestCase: Gen[TestCase] = for {
    fs                  <- Gen.oneOf(FilingStatus.values.toSeq)
    qualifiedIncome     <- Gen.chooseNum(0, 50000)
    ordinaryIncomeNonSS <- Gen.chooseNum(0, 50000)
    ss                  <- Gen.chooseNum(0, 50000)
  } yield TestCase(
    filingStatus = fs,
    qualifiedIncome = qualifiedIncome.tm,
    ordinaryIncomeNonSS = ordinaryIncomeNonSS.tm,
    socSec = ss.tm
  )

  def testCases: List[TestCase] = Gen
    .listOfN(count, genTestCase)
    .sample
    .get
}
