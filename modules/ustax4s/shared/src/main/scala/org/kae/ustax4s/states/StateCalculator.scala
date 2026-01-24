package org.kae.ustax4s.states

import cats.syntax.all.*
import org.kae.ustax4s.federal.FedCalcResults

// Note: It can't be IncomeScenario => StateCalcResults
// because we cannot, in general, change the scenario without
// also re-calculating the FedCalcResults.
type StateCalculator = FedCalcResults => StateCalcResults

object StateCalculator:
  def apply(
    state: State, 
    statePersonProperties: StatePersonProperties
  ): Option[StateCalculator] =
    StateRegime.of(state) match
      case NilStateRegime                 => None
      case regime: FlatStateRegime        => regime.calculator(statePersonProperties).some
      case regime: ProgressiveStateRegime => regime.calculator(statePersonProperties).some
    end match
  end apply

end StateCalculator
