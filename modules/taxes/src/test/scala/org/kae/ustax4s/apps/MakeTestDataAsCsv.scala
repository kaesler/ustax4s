package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.Kevin
import org.kae.ustax4s.inretirement.TaxInRetirement

object MakeTestDataAsCsv extends App {

  import TestDataGeneration._

  private val year = Year.of(2021)

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
