package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.Kevin
import org.kae.ustax4s.inretirement.TaxInRetirement

object MakeTestDataForTypeScript extends App {

  import TestDataGeneration._

  val year = Year.of(2021)

  testCases.foreach { case TestCase(fs, qInv, i401k, ss) =>
    val tax = TaxInRetirement.federalTaxDue(
      year = year,
      birthDate = Kevin.birthDate,
      filingStatus = fs,
      socSec = ss,
      ordinaryIncomeNonSS = i401k,
      qualifiedIncome = qInv
    )
    val status = fs match {
      case HeadOfHousehold => "FilingStatus.HOH"
      case Single          => "FilingStatus.Single"
    }
    println(
      s"  { filingStatus: $status, socSec: $ss, ordinaryIncomeNonSS: $i401k, qualifiedIncome: $qInv, tax: $tax },"
    )
  }
}
