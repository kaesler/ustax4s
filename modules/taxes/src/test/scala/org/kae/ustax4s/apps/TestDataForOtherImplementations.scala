package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.{FilingStatus, IntMoneySyntax, Kevin, TMoney}
import org.scalacheck.Gen

// Create test data for the other implementations(TypeScript, Haskell, Purescript)
object TestDataForOtherImplementations extends App with IntMoneySyntax {

  private val year  = Year.of(2021)
  private val count = 2000

  private final case class TestCase(
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

  private val testCases = Gen
    .listOfN(count, genTestCase)
    .sample
    .get

  println("filingStatus,socialSecurityBenefits,incomeFrom401k,qualifiedInvestmentIncome,tax")
  testCases.foreach { case TestCase(fs, qInv, i401k, ss) =>
    val tax = TaxInRetirement.federalTaxDue(
      year = year,
      birthDate = Kevin.birthDate,
      filingStatus = fs,
      socSec = ss,
      incomeFrom401kEtc = i401k,
      qualifiedInvestmentIncome = qInv
    )
    val status = fs.entryName
    println(
      s"$status,$ss,$i401k,$qInv,$tax"
    )
  }
}
