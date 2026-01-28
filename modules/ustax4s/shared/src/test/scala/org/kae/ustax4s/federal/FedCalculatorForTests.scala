package org.kae.ustax4s.federal

import java.time.{LocalDate, Year}
import org.kae.ustax4s.federal.*
import org.kae.ustax4s.money.*
import org.kae.ustax4s.state_ma.StateMATaxCalculator
import org.kae.ustax4s.{FilingStatus, IncomeScenario}

/** Simplified interface to 1040 calculations.
  * Assume: No deductions credits or other complications.
  * Only used in tests now.
  */
object FedCalculatorForTests:

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
      .fedCalculator(birthDate, personalExemptions)
      .apply(
        IncomeScenario(
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
      .fedCalculator(
        birthDate,
        personalExemptions
      )
      .apply(
        IncomeScenario(
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
      .fedCalculator(birthDate, personalExemptions)
      .apply(
        IncomeScenario(
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
      )(
        birthDate,
        personalExemptions
      )(
        IncomeScenario(
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

  def stateMATaxPayable(
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
  end stateMATaxPayable

end FedCalculatorForTests
