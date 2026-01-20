package org.kae.ustax4s.calculators

import org.kae.ustax4s.money.TaxOutcome

case class FedAndStateCalcResults(
  fedOutcome: TaxOutcome,
  stateOutcome: TaxOutcome = TaxOutcome.zero
):
  lazy val netOutcome: TaxOutcome = fedOutcome + stateOutcome
end FedAndStateCalcResults
