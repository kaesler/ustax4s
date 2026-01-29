package org.kae.ustax4s.calculators

import java.time.{LocalDate, Year}
import org.kae.ustax4s.federal.{BoundFedRegime, FedCalcResults, FedCalculator}
import org.kae.ustax4s.states.{State, StatePersonProperties}
import org.kae.ustax4s.{FilingStatus, IncomeScenario}

// Expressed as successive partial application of a curried function.
object Calculators:

  private def fedCalculator(
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

  private def fedAndStateCalculator(
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int,
    state: State,
    stateQualifiedDependents: Int
    // TODO
    // We may need to include more, e.g.
    //   - pensions not taxed by state
  ): FedAndStateCalculator =
    BoundFedRegime.forAnyYear(
      year,
      estimatedAnnualInflationFactor,
      filingStatus
    )(
      birthDate,
      personalExemptions
    )(
      state,
      StatePersonProperties(stateQualifiedDependents)
    )

  end fedAndStateCalculator

  def calcFedAndState(
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int,
    state: State,
    // Inject state-only inputs here
    stateQualifiedDependents: Int,
    scenario: IncomeScenario
  ): FedAndStateCalcResults =
    fedAndStateCalculator(
      year,
      estimatedAnnualInflationFactor,
      filingStatus,
      birthDate,
      personalExemptions,
      state,
      stateQualifiedDependents
    )(scenario)
  end calcFedAndState

end Calculators
