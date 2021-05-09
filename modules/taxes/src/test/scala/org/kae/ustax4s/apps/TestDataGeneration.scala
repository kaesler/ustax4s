package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.{FilingStatus, IntMoneySyntax, Kevin, TMoney}
import org.scalacheck.Gen

// Create test data for the other implementations(TypeScript, Haskell, Purescript)
object TestDataGeneration extends IntMoneySyntax {

  private val year  = Year.of(2021)
  private val count = 2000

  final case class TestCase(
    filingStatus: FilingStatus,
    investmentIncome: TMoney,
    incomeFrom401k: TMoney,
    ss: TMoney
  )

  private val genTestCase: Gen[TestCase] = for {
    fs                        <- Gen.oneOf(FilingStatus.values)
    qualifiedInvestmentIncome <- Gen.chooseNum(0, 50000)
    incomeFrom401k            <- Gen.chooseNum(0, 50000)
    ss                        <- Gen.chooseNum(0, 50000)
  } yield TestCase(
    filingStatus = fs,
    investmentIncome = qualifiedInvestmentIncome.tm,
    incomeFrom401k = incomeFrom401k.tm,
    ss = ss.tm
  )

  def testCases: List[TestCase] = Gen
    .listOfN(count, genTestCase)
    .sample
    .get
}
