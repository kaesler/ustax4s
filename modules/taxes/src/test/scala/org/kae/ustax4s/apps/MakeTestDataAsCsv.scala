package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.Kevin
import org.kae.ustax4s.inretirement.TaxInRetirement

object MakeTestDataAsCsv extends App {

  import TestDataGeneration._

  private val year = Year.of(2021)

  println("filingStatus,socialSecurityBenefits,ordinaryIncomeNonSS,qualifiedIncome,tax")
  testCases.foreach { case TestCase(fs, qi, oi, ss) =>
    val tax = TaxInRetirement.federalTaxDue(
      year = year,
      birthDate = Kevin.birthDate,
      filingStatus = fs,
      socSec = ss,
      ordinaryIncomeNonSS = oi,
      qualifiedIncome = qi
    )
    val status = fs.entryName
    println(
      s"$status,$ss,$oi,$qi,$tax"
    )
  }
}
