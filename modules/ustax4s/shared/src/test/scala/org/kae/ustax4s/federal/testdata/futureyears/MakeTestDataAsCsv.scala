package org.kae.ustax4s.federal.testdata.futureyears

import cats.implicits.*
import org.kae.ustax4s.federal.FedCalculatorForTests

object MakeTestDataAsCsv:
  import TestDataGeneration.*

  def main(args: Array[String]): Unit =
    println(
      "regime,year,estimatedAnnualInflationFactor,birthDate,filingStatus,dependents,socSec,ordinaryIncomeNonSS," +
        "qualifiedIncome,itemizedDeductions,federalTaxDue,stateTaxDue"
    )
    testCases.foreach:
      case tc @ TestCaseInputs(regime, year, estimate, bd, fs, ds, ss, oi, qi, itm) =>
        val federalTaxDue = FedCalculatorForTests.federalTaxPayableForFutureYear(
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
        val stateTaxDue = FedCalculatorForTests.stateMATaxPayable(
          year = year,
          birthDate = bd,
          filingStatus = fs,
          dependents = ds,
          oi + qi
        )
        println(
          s"$regime,${year.getValue},$estimate,${bd.toString},${fs.show},$ds,$ss,$oi,$qi,$itm,$federalTaxDue,$stateTaxDue"
        )
  end main

end MakeTestDataAsCsv
