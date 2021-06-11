package org.kae.ustax4s.inretirement.testdata

import java.time.Year
import org.kae.ustax4s.moneyold.given
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.{FilingStatus, kevin}

object MakeTestDataAsCsv extends App:
  import TestDataGeneration.*

  private val year = Year.of(2021)

  println(
    "filingStatus,dependents,socSec,ordinaryIncomeNonSS,qualifiedIncome,federalTaxDue,stateTaxDue"
  )
  testCases.foreach { case TestCaseInputs(fs, ds, ss, oi, qi) =>
    val federalTaxDue = TaxInRetirement.federalTaxDue(
      year = year,
      birthDate = kevin.Kevin.birthDate,
      filingStatus = fs,
      socSec = ss,
      ordinaryIncomeNonSS = oi,
      qualifiedIncome = qi
    )
    val stateTaxDue = TaxInRetirement.stateTaxDue(
      year = year,
      birthDate = kevin.Kevin.birthDate,
      filingStatus = fs,
      dependents = ds,
      oi + qi
    )
    println(
      s"${fs.entryName},$ds,$ss,$oi,$qi,$federalTaxDue,$stateTaxDue"
    )
  }

end MakeTestDataAsCsv
