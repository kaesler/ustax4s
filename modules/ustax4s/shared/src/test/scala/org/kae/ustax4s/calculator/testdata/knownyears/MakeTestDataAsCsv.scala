package org.kae.ustax4s.calculator.testdata.knownyears

import cats.implicits.*
import org.kae.ustax4s.calculator.TaxCalculator

object MakeTestDataAsCsv extends App:
  import TestDataGeneration.*

  println(
    "year,birthDate,filingStatus,dependents,socSec,ordinaryIncomeNonSS," +
      "qualifiedIncome,itemizedDeductions,federalTaxDue,stateTaxDue"
  )
  testCases.foreach { case tc @ TestCaseInputs(year, bd, fs, ds, ss, oi, qi, itm) =>
    val federalTaxDue = TaxCalculator.federalTaxPayable(
      year = year,
      birthDate = bd,
      filingStatus = fs,
      tc.personalExemptions,
      socSec = ss,
      ordinaryIncomeNonSS = oi,
      qualifiedIncome = qi,
      itemizedDeductions = itm
    )
    val stateTaxDue = TaxCalculator.stateTaxPayable(
      year = year,
      birthDate = bd,
      filingStatus = fs,
      dependents = ds,
      oi + qi
    )
    println(
      s"${year.getValue},${bd.toString},${fs.show},$ds,$ss,$oi,$qi,$itm,$federalTaxDue,$stateTaxDue"
    )
  }

end MakeTestDataAsCsv
