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

  def federalTaxPayableForFutureYear(
    regime: FedRegime,
    futureYear: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    // Self plus dependents
    personalExemptions: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  ): TaxPayable =
    BoundFedRegime
      .forFutureYear(
        regime,
        futureYear,
        estimatedAnnualInflationFactor,
        filingStatus
      )
      .calculator
      .apply(
        CalcInput(
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )
      )
      .taxPayable
      .rounded
  end federalTaxPayableForFutureYear

  def federalTaxResultsForAnyYear(
    estimatedAnnualInflationFactor: Double,
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    // Self plus dependents
    personalExemptions: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  ): FedCalcResults =
    BoundFedRegime
      .forAnyYear(year, estimatedAnnualInflationFactor, filingStatus)
      .calculator
      .apply(
        CalcInput(
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )
      )
  end federalTaxResultsForAnyYear

  def federalTaxResults(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    // Self plus dependents
    personalExemptions: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  ): FedCalcResults =
    BoundFedRegime
      .forKnownYear(
        year,
        filingStatus
      )
      .calculator
      .apply(
        CalcInput(
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )
      )
  end federalTaxResults

  def federalTaxResultsForFutureYear(
    regime: FedRegime,
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    // Self plus dependents
    personalExemptions: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  ): FedCalcResults =
    BoundFedRegime
      .forFutureYear(
        regime,
        year,
        estimatedAnnualInflationFactor,
        filingStatus
      )
      .calculator
      .apply(
        CalcInput(
          birthDate,
          personalExemptions,
          socSec,
          ordinaryIncomeNonSS,
          qualifiedIncome,
          itemizedDeductions
        )
      )
  end federalTaxResultsForFutureYear

  def federalTaxPayable(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    // Self plus dependents
    personalExemptions: Int,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedIncome: TaxableIncome,
    itemizedDeductions: Deduction
  ): TaxPayable =
    federalTaxResults(
      year,
      filingStatus,
      birthDate,
      personalExemptions,
      socSec,
      ordinaryIncomeNonSS,
      qualifiedIncome,
      itemizedDeductions
    ).taxPayable.rounded
  end federalTaxPayable

  // Note: for tests only
  def federalTaxPayableUsingForm1040(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: Income,
    ordinaryIncomeNonSS: Income,
    qualifiedDividends: TaxableIncome,
    verbose: Boolean
  ): TaxPayable =
    val boundRegime = BoundFedRegime.forKnownYear(
      year,
      filingStatus
    )

    val form = Form1040(
      filingStatus,
      taxableIraDistributions = ordinaryIncomeNonSS,
      socialSecurityBenefits = socSec,
      // The rest not applicable in retirement.
      standardDeduction = boundRegime.standardDeduction(birthDate),
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
        yv.ordinaryRateFunctions(filingStatus),
        yv.qualifiedRateFunctions(filingStatus)
      )
      .rounded
  end federalTaxPayableUsingForm1040

  def stateTaxPayable(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
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
        filingStatus,
        birthDate,
        dependents
      )(massachusettsGrossIncome)
      .rounded
  end stateTaxPayable

end TaxCalculator
