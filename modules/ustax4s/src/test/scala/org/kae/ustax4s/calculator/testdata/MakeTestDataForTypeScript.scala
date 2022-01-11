package org.kae.ustax4s.calculator.testdata

import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.calculator.TaxCalculator

object MakeTestDataForTypeScript extends App:

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
    val status = fs match
      case HeadOfHousehold => "FilingStatus.HOH"
      case Single          => "FilingStatus.Single"
    println(
      s"  { regime = ${regime.name}, year = ${year.getValue}, birthDate: $bd, " +
        s"dependents: $deps, filingStatus: $status, socSec: $ss, " +
        s"ordinaryIncomeNonSS: $oi, qualifiedIncome: $qi, " +
        s"itemizedDeductions: $itm, " +
        s"federalTaxDue: $federalTaxDue, stateTaxDue: $stateTaxDue },"
    )
  }