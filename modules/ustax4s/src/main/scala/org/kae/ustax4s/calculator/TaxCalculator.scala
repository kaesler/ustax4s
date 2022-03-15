package org.kae.ustax4s.calculator

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.*
import org.kae.ustax4s.federal.forms.Form1040
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.*
import org.kae.ustax4s.state_ma.StateMATaxCalculator

/** Simplified interface to 1040 calculations. Assume: No deductions credits or other complications.
  */
object TaxCalculator:

  def federalTaxDue(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    // Self plus dependents
    personalExemptions: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  ): TaxPayable =
    BoundRegime
      .forKnownYear(
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
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedDividends: TaxableIncome,
    verbose: Boolean
  ): TaxPayable =
    val boundRegime = BoundRegime.forKnownYear(
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
      childTaxCredit = TaxCredit.zero,
      wages = Income.zero,
      taxExemptInterest = Income.zero,
      taxableInterest = Income.zero,
      qualifiedDividends = qualifiedDividends,
      ordinaryDividends = qualifiedDividends
    )
    if verbose then println(form.showValues)

    val yv = YearlyValues.of(year).get
    Form1040
      .totalFederalTax(
        form,
        yv.ordinaryBrackets(filingStatus),
        yv.qualifiedBrackets(filingStatus)
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
    massachusettsGrossIncome: Income
  ): TaxPayable =
    StateMATaxCalculator
      .taxDue(
        year,
        birthDate,
        dependents,
        filingStatus
      )(massachusettsGrossIncome)
      .rounded
