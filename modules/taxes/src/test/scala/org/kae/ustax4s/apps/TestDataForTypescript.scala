package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.{FilingStatus, IntMoneySyntax, SimpleTaxInRetirement, TMoney}
import org.scalacheck.Gen

object TestDataForTypescript extends App with IntMoneySyntax {

  private val year = Year.of(2021)

  private final case class TestCase(
    filingStatus: FilingStatus,
    investmentIncome: TMoney,
    incomeFrom401k: TMoney,
    ss: TMoney
  )
  private val genTestCase: Gen[TestCase] = for {
    fs <- Gen.oneOf(FilingStatus.values)
    qualifiedInvestmentIncome <- Gen.chooseNum(0, 50000)
    incomeFrom401k <- Gen.chooseNum(0, 50000)
    ss <- Gen.chooseNum(0, 50000)
  } yield TestCase(
    filingStatus = fs,
    investmentIncome = qualifiedInvestmentIncome.tm,
    incomeFrom401k = incomeFrom401k.tm,
    ss = ss.tm
  )

  Gen
    .listOfN(1000, genTestCase)
    .sample
    .get
    .foreach {
      case TestCase(fs, qInv, i401k, ss) =>
        val tax = SimpleTaxInRetirement.taxDue(
          year = year,
          filingStatus = fs,
          socSec = ss,
          incomeFrom401kEtc = i401k,
          qualifiedInvestmentIncome = qInv
        )
        val status = fs match {
          case HeadOfHousehold => "FilingStatus.HOH"
          case Single => "FilingStatus.Single"
        }
        println(
          s"  { filingStatus: $status, socialSecurityBenefits: $ss, incomeFrom401k: $i401k, qualifiedInvestmentIncome: $qInv, tax: $tax },"
        )
    }
}
