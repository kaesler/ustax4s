package org.kae.ustax4s.calculator

import cats.Show
import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.*
import org.kae.ustax4s.federal.forms.Form1040
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.state_ma.StateMATaxCalculator

/** Simplified interface to 1040 calculations. Assume: No deductions credits or other complications.
  */
object TaxCalculator:

  def federalTaxDue(
    regime: Regime,
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    // Self plus dependents
    personalExemptions: Int,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    itemizedDeductions: Money
  ): Money =
    BoundRegime
      .create(
        regime,
        year,
        birthDate,
        filingStatus,
        personalExemptions
      )
      .calculator
      .federalTaxResults(
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .taxDue
      .rounded

  // Note: for tests only
  def federalTaxDueUsingForm1040(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedDividends: Money,
    verbose: Boolean
  ): Money =
    val regime = Trump
    val boundRegime = BoundRegime.create(
      regime,
      year,
      birthDate,
      filingStatus,
      personalExemptions = 2
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
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    dependents: Int,
    // Excludes SocSec. So it is
    //  - earned wages
    //  - interest
    //  - dividends
    //  - capital gains
    massachusettsGrossIncome: Money
  ): Money =
    StateMATaxCalculator
      .taxDue(
        year,
        birthDate,
        dependents,
        filingStatus
      )(massachusettsGrossIncome)
      .rounded
