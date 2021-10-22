package org.kae.ustax4s.calculator.testdata

import java.time.Year
import org.kae.ustax4s.federal.Trump
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.{FilingStatus, kevin}

object MakeTestDataAsCsv extends App:
  import TestDataGeneration.*

  private val regime             = Trump
  private val year               = Year.of(2021)
  private val personalExemptions = 0
  private val itemizedDeductions = 0

  println(
    "filingStatus,dependents,socSec,ordinaryIncomeNonSS,qualifiedIncome,federalTaxDue,stateTaxDue"
  )
  testCases.foreach { case TestCaseInputs(fs, ds, ss, oi, qi) =>
    val federalTaxDue = TaxCalculator.federalTaxDue(
      regime,
      year = year,
      birthDate = kevin.Kevin.birthDate,
      filingStatus = fs,
      personalExemptions,
      socSec = ss,
      ordinaryIncomeNonSS = oi,
      qualifiedIncome = qi,
      itemizedDeductions
    )
    val stateTaxDue = TaxCalculator.stateTaxDue(
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
