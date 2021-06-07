package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.{FilingStatus, Kevin}
import org.kae.ustax4s.inretirement.TaxInRetirement

object MakeTestDataAsCsv extends App:
  import TestDataGeneration.*

  private val year = Year.of(2021)

  println("filingStatus,socSec,ordinaryIncomeNonSS,qualifiedIncome,federalTaxDue,stateTaxDue")
  testCases.foreach { case TestCase(fs, qi, oi, ss) =>
    val federalTaxDue = TaxInRetirement.federalTaxDue(
      year = year,
      birthDate = Kevin.birthDate,
      filingStatus = fs,
      socSec = ss,
      ordinaryIncomeNonSS = oi,
      qualifiedIncome = qi
    )
    val stateTaxDue = TaxInRetirement.stateTaxDue(
      year = year,
      birthDate = Kevin.birthDate,
      filingStatus = fs,
      dependents = if fs == FilingStatus.HeadOfHousehold then 1 else 0,
      oi
    )
    println(
      s"${fs.entryName},$ss,$oi,$qi,$federalTaxDue,$stateTaxDue"
    )
  }

end MakeTestDataAsCsv
