package org.kae.ustax4s.calculator.testdata

import org.kae.ustax4s.calculator.TaxCalculator

object MakeTestDataForHaskell extends App:

  import TestDataGeneration.*

  testCases.foreach { case tc @ TestCaseInputs(year, bd, fs, deps, ss, oi, qi, itm) =>
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
      dependents = deps,
      massachusettsGrossIncome = oi + qi
    )
    val bdString = s"fromGregorian ${bd.getYear} ${bd.getMonthValue} ${bd.getDayOfMonth}"
    println(
      s"  TestCase { year = ${year.getValue}, birthDate = $bdString, dependents = $deps, filingStatus = $fs, socSec = makeFromInt $ss, " +
        s"ordinaryIncomeNonSS = makeFromInt $oi, qualifiedIncome = makeFromInt $qi, " +
        s"itemizedDeductions = makeFromInt $itm, " +
        s"expectedFederalTax = makeFromInt $federalTaxDue, expectedStateTax = makeFromInt $stateTaxDue },"
    )
  }

end MakeTestDataForHaskell
