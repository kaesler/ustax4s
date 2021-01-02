package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.{FilingStatus, IntMoneySyntax, SimpleTaxInRetirement, TMoney}
import org.scalacheck.Gen

object TestDataForTypescript extends App with IntMoneySyntax {

  private val year = Year.of(2021)

  private final case class TestCase(
    filingStatus: FilingStatus,
    incomeFrom401k: TMoney,
    ss: TMoney
  )
  private val genTestCase: Gen[TestCase] = for {
    fs <- Gen.oneOf(FilingStatus.values)
    incomeFrom401k <- Gen.chooseNum(0, 50000)
    ss <- Gen.chooseNum(0, 50000)
  } yield TestCase(
    filingStatus = fs,
    incomeFrom401k = incomeFrom401k.tm,
    ss = ss.tm
  )

  Gen
    .listOfN(1000, genTestCase)
    .sample
    .get
    .foreach {
      case TestCase(fs, i, ss) =>
        val tax = SimpleTaxInRetirement.taxDue(year, fs, i, ss)
        val status = fs match {
          case HeadOfHousehold => "FilingStatus.HOH"
          case Single => "FilingStatus.Single"
        }
        println(
          s"  { filingStatus: $status, incomeFrom401k: $i, socialSecurityBenefits: $ss, tax: $tax },"
        )
    }
}
