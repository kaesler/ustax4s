package org.kae.ustax4s.calculator.testdata.futureyears

import org.kae.ustax4s.calculator.TaxCalculator

object MakeTestDataForPureScript extends App:
  import TestDataGeneration.*

  testCases.foreach {
    case tc @ TestCaseInputs(regime, year, estimate, bd, fs, deps, ss, oi, qi, itm) =>
      val federalTaxDue = TaxCalculator.federalTaxPayableForFutureYear(
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

      val stateTaxDue = TaxCalculator.stateTaxPayable(
        year = year,
        birthDate = bd,
        filingStatus = fs,
        dependents = deps,
        massachusettsGrossIncome = oi + qi
      )
      val bdRep   = s"(unsafeMakeDate ${bd.getYear} ${bd.getMonthValue} ${bd.getDayOfMonth})"
      val yearRep = s"(unsafeMakeYear ${year.getValue})"
      println(
        s"  TestCase { regime: $regime, year: $yearRep, " +
          s"estimatedAnnualInflationFactor: $estimate, birthDate: $bdRep, " +
          s"personalExemptions: ${tc.personalExemptions}, filingStatus: $fs, " +
          s"socSec: makeFromInt $ss, ordinaryIncomeNonSS: makeFromInt $oi, " +
          s"qualifiedIncome: makeFromInt $qi, " +
          s"itemizedDeductions: makeFromInt $itm, " +
          s"federalTaxDue: makeFromInt $federalTaxDue, stateTaxDue: makeFromInt" +
          s" $stateTaxDue },"
      )
  }
