package org.kae.ustax4s.inretirement.testdata

import java.time.Year
import org.kae.ustax4s.moneyold.given
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.kevin.Kevin

object MakeTestDataForHaskell extends App:

  import TestDataGeneration.*

  val year = Year.of(2021)
  val age  = 66

  testCases.foreach { case TestCaseInputs(fs, deps, ss, oi, qi) =>
    val federalTaxDue = TaxInRetirement.federalTaxDue(
      year = year,
      birthDate = Kevin.birthDate,
      filingStatus = fs,
      socSec = ss,
      ordinaryIncomeNonSS = oi,
      qualifiedIncome = qi
    )
    val stateTaxDue = TaxInRetirement.stateTaxDue(
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
