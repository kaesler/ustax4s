package org.kae.ustax4s.calculator.testdata

import java.time.LocalDate
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
    val bdRep   = s"(unsafeMakeDate ${bd.getYear} ${bd.getMonthValue} ${bd.getDayOfMonth})"
    val yearRep = s"(unsafeMakeYear ${year.getValue})"
    println(
      s"  TestCase { regime: ${regime.name}, year: $yearRep, birthDate: $bdRep, " +
        s"personalExemptions: ${tc.personalExemptions}, filingStatus: $fs, " +
        s"socSec: makeFromInt $ss, ordinaryIncomeNonSS: makeFromInt $oi, qualifiedIncome: makeFromInt $qi, " +
        s"itemizedDeductions: makeFromInt $itm, " +
        s"federalTaxDue: makeFromInt $federalTaxDue, stateTaxDue: makeFromInt $stateTaxDue },"
    )
  }
