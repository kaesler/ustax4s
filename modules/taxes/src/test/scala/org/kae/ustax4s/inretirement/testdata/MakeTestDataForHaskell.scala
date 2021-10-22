package org.kae.ustax4s.inretirement.testdata

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.Trump
import org.kae.ustax4s.inretirement.TaxCalculator
import org.kae.ustax4s.kevin.Kevin

object MakeTestDataForHaskell extends App:

  import TestDataGeneration.*

  private val regime             = Trump
  private val year               = Year.of(2021)
  private val age                = 66
  private val personalExemptions = 0
  private val itemizedDeductions = 0

  testCases.foreach { case TestCaseInputs(fs, deps, ss, oi, qi) =>
    val federalTaxDue = TaxCalculator.federalTaxDue(
      regime,
      year = year,
      birthDate = Kevin.birthDate,
      filingStatus = fs,
      personalExemptions,
      socSec = ss,
      ordinaryIncomeNonSS = oi,
      qualifiedIncome = qi,
      itemizedDeductions
    )
    val stateTaxDue = TaxCalculator.stateTaxDue(
      year = year,
      birthDate = Kevin.birthDate,
      filingStatus = fs,
      dependents = deps,
      massachusettsGrossIncome = oi + qi
    )
    println(
      s"  TestCase { age = $age, dependents = $deps, filingStatus = $fs, socSec = $ss, " +
        s"ordinaryIncomeNonSS = $oi, qualifiedIncome = $qi, " +
        s"expectedFederalTax: $federalTaxDue, expectedStateTax = $stateTaxDue },"
    )
  }

end MakeTestDataForHaskell
