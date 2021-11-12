package org.kae.ustax4s.calculator.testdata

import org.kae.ustax4s.calculator.TaxCalculator

object MakeTestDataForPureScript extends App:
  import TestDataGeneration.*

  testCases.foreach { case tc @ TestCaseInputs(regime, year, bd, fs, deps, ss, oi, qi, itm) =>
    val federalTaxDue = TaxCalculator.federalTaxDue(
      regime = regime,
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
    println(
      s"  TestCase { regime = ${regime.name}, year = ${year.getValue}, birthDate: $bd, dependents: $deps, filingStatus: $fs, socSec: $ss, " +
        s"ordinaryIncomeNonSS: $oi, qualifiedIncome: $qi, " +
        s"itemizedDeductions: $itm, " +
        s"federalTaxDue: $federalTaxDue, stateTaxDue: $stateTaxDue },"
    )
  }
