package org.kae.ustax4s.calculator.testdata.knownyears

import org.kae.ustax4s.calculator.TaxCalculator

object MakeTestDataForPureScript extends App:
  import TestDataGeneration.*

  testCases.foreach { case tc @ TestCaseInputs(year, bd, fs, deps, ss, oi, 
  qi, itm) =>
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
    val bdRep   = s"(unsafeMakeDate ${bd.getYear} ${bd.getMonthValue} ${bd
      .getDayOfMonth})"
    val yearRep = s"(unsafeMakeYear ${year.getValue})"
    println(
      s"  TestCase { year: $yearRep, birthDate: $bdRep, " +
        s"personalExemptions: ${tc.personalExemptions}, filingStatus: $fs, " +
        s"socSec: makeFromInt $ss, ordinaryIncomeNonSS: makeFromInt $oi, " +
        s"qualifiedIncome: makeFromInt $qi, " +
        s"itemizedDeductions: makeFromInt $itm, " +
        s"federalTaxDue: makeFromInt $federalTaxDue, stateTaxDue: makeFromInt" +
        s" $stateTaxDue },"
    )
  }
