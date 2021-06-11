package org.kae.ustax4s.inretirement

import java.time.Year
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.federal.{TaxRates, forms}
import org.kae.ustax4s.federal.forms.Form1040
import org.kae.ustax4s.kevin.Kevin

object MyTaxInRetirement:

  def federalTaxDue(
    year: Year,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money
  ): Money =
    TaxInRetirement.federalTaxDue(
      year,
      Kevin.birthDate,
      Kevin.filingStatus(year),
      socSec,
      ordinaryIncomeNonSS,
      qualifiedIncome
    )

  def federalTaxDueUsingForm1040(
    year: Year,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedDividends: Money,
    verbose: Boolean
  ): Money = {
    val filingStatus = Kevin.filingStatus(year)
    val myRates = TaxRates.of(
      year,
      filingStatus,
      Kevin.birthDate
    )

    val form = forms.Form1040(
      filingStatus,
      rates = myRates,
      taxableIraDistributions = ordinaryIncomeNonSS,
      socialSecurityBenefits = socSec,
      // The rest not applicable in retirement.
      standardDeduction = myRates.standardDeduction,
      schedule1 = None,
      schedule3 = None,
      schedule4 = None,
      schedule5 = None,
      childTaxCredit = Money.zero,
      wages = Money.zero,
      taxExemptInterest = Money.zero,
      taxableInterest = Money.zero,
      qualifiedDividends = qualifiedDividends,
      ordinaryDividends = qualifiedDividends
    )
    if (verbose) {
      println(form.showValues)
    }

    myRates.totalTax(form).rounded
  }

  def stateTaxDue(
    year: Year,
    // Excludes SocSec. So it is
    //  - earned wages
    //  - interest
    //  - dividends
    //  - capital gains
    massachusettsGrossIncome: Money
  ): Money =
    TaxInRetirement.stateTaxDue(
      year,
      Kevin.birthDate,
      Kevin.filingStatus(year),
      Kevin.numberOfMassachusettsDependents(year),
      massachusettsGrossIncome
    )
