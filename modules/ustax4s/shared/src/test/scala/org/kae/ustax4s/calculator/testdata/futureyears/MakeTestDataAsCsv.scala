package org.kae.ustax4s.calculator.testdata.futureyears

import cats.implicits.*
import org.kae.ustax4s.calculator.FedTaxCalculatorForTests

object MakeTestDataAsCsv extends App:
  import TestDataGeneration.*

  println(
    "regime,year,estimatedAnnualInflationFactor,birthDate,filingStatus,dependents,socSec,ordinaryIncomeNonSS," +
      "qualifiedIncome,itemizedDeductions,federalTaxDue,stateTaxDue"
  )
  testCases.foreach {
    case tc @ TestCaseInputs(regime, year, estimate, bd, fs, ds, ss, oi, qi, itm) =>
      val federalTaxDue = FedTaxCalculatorForTests.federalTaxPayableForFutureYear(
        regime,
        futureYear = year,
        estimatedAnnualInflationFactor = estimate,
        birthDate = bd,
        filingStatus = fs,
        tc.personalExemptions,
        socSec = ss,
        ordinaryIncomeNonSS = oi,
        qualifiedIncome = qi,
        itemizedDeductions = itm
      )
      val stateTaxDue = FedTaxCalculatorForTests.stateMATaxPayable(
        year = year,
        birthDate = bd,
        filingStatus = fs,
        dependents = ds,
        oi + qi
      )
      println(
        s"$regime,${year.getValue},$estimate,${bd.toString},${fs.show},$ds,$ss,$oi,$qi,$itm,$federalTaxDue,$stateTaxDue"
      )
  }

end MakeTestDataAsCsv
