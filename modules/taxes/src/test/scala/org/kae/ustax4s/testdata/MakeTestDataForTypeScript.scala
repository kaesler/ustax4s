package org.kae.ustax4s
package testdata

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.Kevin
import org.kae.ustax4s.inretirement.TaxInRetirement

object MakeTestDataForTypeScript extends App:

  import TestDataGeneration.*

  val year = Year.of(2021)

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
    val status = fs match
      case HeadOfHousehold => "FilingStatus.HOH"
      case Single          => "FilingStatus.Single"
    println(
      s"  { filingStatus: $status, socSec: $ss, ordinaryIncomeNonSS: $oi," +
        s" qualifiedIncome: $qi, federalTaxDue: $federalTaxDue, stateTaxDue: $stateTaxDue },"
    )
  }
