package org.kae.ustax4s.federal.testdata.futureyears

import org.kae.ustax4s.federal.FedCalculatorForTests

object MakeTestDataForHaskell extends App:

  import TestDataGeneration.*

  testCases.foreach {
    case tc @ TestCaseInputs(regime, year, estimate, bd, fs, deps, ss, oi, qi, itm) =>
      val federalTaxDue = FedCalculatorForTests.federalTaxPayableForFutureYear(
        regime = regime,
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
        dependents = deps,
        massachusettsGrossIncome = oi + qi
      )
      val bdString = s"fromGregorian ${bd.getYear} ${bd.getMonthValue} ${bd.getDayOfMonth}"
      println(
        s"  TestCase { regime = $regime, year = ${year.getValue}, estimatedAnnualInflationFactor = $estimate, birthDate = $bdString, dependents = $deps, filingStatus = $fs, socSec = makeFromInt $ss, " +
          s"ordinaryIncomeNonSS = makeFromInt $oi, qualifiedIncome = makeFromInt $qi, " +
          s"itemizedDeductions = makeFromInt $itm, " +
          s"expectedFederalTax = makeFromInt $federalTaxDue, expectedStateTax = makeFromInt $stateTaxDue },"
      )
  }

end MakeTestDataForHaskell
