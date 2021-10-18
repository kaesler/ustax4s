package org.kae.ustax4s.inretirement.testdata

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.federal.Trump
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.kevin.Kevin

object MakeTestDataForTypeScript extends App:

  import TestDataGeneration.*

  val year = Year.of(2021)
  val age  = 66

  testCases.foreach { case TestCaseInputs(fs, deps, ss, oi, qi) =>
    val federalTaxDue = TaxInRetirement.federalTaxDue(
      regime = Trump,
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
      s"  { age: $age, dependents: $deps, filingStatus: $status, socSec: $ss, " +
        s"ordinaryIncomeNonSS: $oi, qualifiedIncome: $qi, " +
        s"federalTaxDue: $federalTaxDue, stateTaxDue: $stateTaxDue },"
    )
  }
