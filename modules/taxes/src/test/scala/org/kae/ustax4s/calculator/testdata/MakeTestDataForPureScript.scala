package org.kae.ustax4s.calculator.testdata

import org.kae.ustax4s.calculator.TaxCalculator
import java.time.LocalDate

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
    val bdRep   = s"(unsafeMakeDate ${bd.getYear} ${bd.getMonthValue} ${bd.getDayOfMonth})"
    val yearRep = s"(unsafeMakeYear ${year.getValue})"
    println(
      s"  TestCase { regime: ${regime.name}, year: $yearRep, birthDate: $bdRep, " +
        s"personalExemptions: ${tc.personalExemptions}, filingStatus: $fs, " +
        s"socSec: $ss.0, ordinaryIncomeNonSS: $oi.0, qualifiedIncome: $qi.0, " +
        s"itemizedDeductions: $itm.0, " +
        s"federalTaxDue: $federalTaxDue.0, stateTaxDue: $stateTaxDue.0 },"
    )
  }
