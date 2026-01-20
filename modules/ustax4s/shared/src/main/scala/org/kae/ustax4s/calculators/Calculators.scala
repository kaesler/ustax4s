package org.kae.ustax4s.calculators

import java.time.{LocalDate, Year}
import org.kae.ustax4s.federal.{BoundFedRegime, FedCalcResults, FedCalculator}
import org.kae.ustax4s.states.State
import org.kae.ustax4s.{FilingStatus, IncomeScenario}

// TODO
// We may need to include state-relevant stuff
//   - pensions not taxed by state

// Expressed as successive partial application of a curried function.
object Calculators:

  def fedCalculator(
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int
  ): FedCalculator =
    BoundFedRegime.forAnyYear(
      year,
      estimatedAnnualInflationFactor,
      filingStatus
    )(
      birthDate,
      personalExemptions
    )
  end fedCalculator

  def calcFed(
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int,
    scenario: IncomeScenario
  ): FedCalcResults =
    fedCalculator(
      year,
      estimatedAnnualInflationFactor,
      filingStatus,
      birthDate,
      personalExemptions
    )(
      scenario
    )
  end calcFed

  def fedAndStateCalculator(
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int,
    state: State
  ): FedAndStateCalculator =
    BoundFedRegime.forAnyYear(
      year,
      estimatedAnnualInflationFactor,
      filingStatus
    )(
      birthDate,
      personalExemptions
    )(
      state
    )

  end fedAndStateCalculator

  def calcFedAndState(
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int,
    state: State,
    scenario: IncomeScenario
  ): FedAndStateCalcResults =
    fedAndStateCalculator(
      year,
      estimatedAnnualInflationFactor,
      filingStatus,
      birthDate,
      personalExemptions,
      state
    )(scenario)
  end calcFedAndState

end Calculators
