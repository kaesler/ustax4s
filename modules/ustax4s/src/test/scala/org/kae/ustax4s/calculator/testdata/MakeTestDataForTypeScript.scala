package org.kae.ustax4s.calculator.testdata

import cats.implicits.*
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.calculator.TaxCalculator

object MakeTestDataForTypeScript extends App:

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
    val status = fs match
      case HeadOfHousehold => "FilingStatus.HOH"
      case Married         => "Married"
      case Single          => "FilingStatus.Single"
    println(
      s"  { year = ${year.getValue}, birthDate: $bd, " +
        s"dependents: $deps, filingStatus: $status, socSec: $ss, " +
        s"ordinaryIncomeNonSS: $oi, qualifiedIncome: $qi, " +
        s"itemizedDeductions: $itm, " +
        s"federalTaxDue: $federalTaxDue, stateTaxDue: $stateTaxDue },"
    )
  }
