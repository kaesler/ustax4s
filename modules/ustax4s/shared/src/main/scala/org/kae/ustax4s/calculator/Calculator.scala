package org.kae.ustax4s.calculator

import java.time.{LocalDate, Year}
import org.kae.ustax4s.federal.{BoundFedRegime, FedCalcResults}
import org.kae.ustax4s.states.State
import org.kae.ustax4s.{FilingStatus, IncomeScenario}

// TODO
// We may need to include state-relevant stuff
//   - pensions not taxed by state
//   - SocSec
//   - should FilingStatus be here?

// Expressed as successive partial application of a curried function.
object Calculator:

  def calcFed(
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int,
    scenario: IncomeScenario
  ): FedCalcResults =
    BoundFedRegime.forAnyYear(
      year,
      estimatedAnnualInflationFactor,
      filingStatus
    )(
      birthDate,
      personalExemptions
    )(
      scenario
    )
  end calcFed

  def calcFedAndState(
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int,
    state: State,
    scenario: IncomeScenario
  ): FedAndStateCalcResults =
    BoundFedRegime.forAnyYear(
      year,
      estimatedAnnualInflationFactor,
      filingStatus
    )(
      birthDate,
      personalExemptions
    )(
      state
    )(
      scenario
    )
  end calcFedAndState

end Calculator
