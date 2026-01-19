package org.kae.ustax4s.states

import org.kae.ustax4s.federal.FedCalcResults

trait StateCalculator() extends (FedCalcResults => StateCalcResults)
