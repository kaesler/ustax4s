package org.kae.ustax4s.calculators

import org.kae.ustax4s.money.TaxOutcome

case class FedAndStateCalcResults(
  fedOutcome: TaxOutcome,
  stateOutcome: TaxOutcome
):
  lazy val netOutcome: TaxOutcome = fedOutcome + stateOutcome
