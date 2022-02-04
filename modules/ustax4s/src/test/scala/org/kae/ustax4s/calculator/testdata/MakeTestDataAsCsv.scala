package org.kae.ustax4s.calculator.testdata

import org.kae.ustax4s.calculator.TaxCalculator

object MakeTestDataAsCsv extends App:
  import TestDataGeneration.*

  println(
    "regime,year,birthDate,filingStatus,dependents,socSec,ordinaryIncomeNonSS,qualifiedIncome,itemizedDeductions,federalTaxDue,stateTaxDue"
  )
  testCases.foreach { case tc @ TestCaseInputs(regime, year, bd, fs, ds, ss, oi, qi, itm) =>
    val federalTaxDue = TaxCalculator.federalTaxDue(
      year = year,
      birthDate = bd,
      filingStatus = fs,
      tc.personalExemptions,
      socSec = ss,
      ordinaryIncomeNonSS = oi,
      qualifiedIncome = qi,
      itemizedDeductions = itm
    )
    val stateTaxDue = TaxCalculator.stateTaxDue(
      year = year,
      birthDate = bd,
      filingStatus = fs,
      dependents = ds,
      oi + qi
    )
    println(
      s"${regime.name},${year.getValue},${bd.toString},${fs.entryName},$ds,$ss,$oi,$qi,$itm,$federalTaxDue,$stateTaxDue"
    )
  }

end MakeTestDataAsCsv
