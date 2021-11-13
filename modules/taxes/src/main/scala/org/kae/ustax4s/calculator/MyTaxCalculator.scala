package org.kae.ustax4s.calculator

import java.time.Year
import org.kae.ustax4s.federal.forms.Form1040
import org.kae.ustax4s.federal.{
  BoundRegime,
  OrdinaryIncomeBrackets,
  QualifiedIncomeBrackets,
  Regime,
  Trump
}
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.Money

object MyTaxCalculator:

  def federalTaxDue(
    regime: Regime,
    year: Year,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    itemizedDeductions: Money
  ): Money =
    TaxCalculator.federalTaxDue(
      regime,
      year,
      Kevin.birthDate,
      Kevin.filingStatus(year),
      Kevin.personalExemptions(year),
      socSec,
      ordinaryIncomeNonSS,
      qualifiedIncome,
      itemizedDeductions
    )

  def federalTaxDueUsingForm1040(
    year: Year,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedDividends: Money,
    verbose: Boolean
  ): Money =
    val filingStatus = Kevin.filingStatus(year)
    val regime       = Trump
    val boundRegime = BoundRegime.create(
      regime,
      year,
      filingStatus,
      Kevin.birthDate,
      Kevin.personalExemptions(year)
    )

    val form = Form1040(
      filingStatus,
      taxableIraDistributions = ordinaryIncomeNonSS,
      socialSecurityBenefits = socSec,
      // The rest not applicable in retirement.
      standardDeduction = boundRegime.standardDeduction,
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
    if verbose then println(form.showValues)

    Form1040
      .totalFederalTax(
        form,
        regime.ordinaryIncomeBrackets(year, filingStatus),
        regime.qualifiedIncomeBrackets(year, filingStatus)
      )
      .rounded
  end federalTaxDueUsingForm1040

  def stateTaxDue(
    year: Year,
    // Excludes SocSec. So it is
    //  - earned wages
    //  - interest
    //  - dividends
    //  - capital gains
    massachusettsGrossIncome: Money
  ): Money =
    TaxCalculator.stateTaxDue(
      year,
      Kevin.birthDate,
      Kevin.filingStatus(year),
      Kevin.numberOfMassachusettsDependents(year),
      massachusettsGrossIncome
    )
