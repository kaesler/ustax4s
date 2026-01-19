package org.kae.ustax4s.calculator

import org.kae.ustax4s.money.TaxOutcome

case class FedAndStateCalcResults(
  fedOutcome: TaxOutcome,
  stateOutcome: TaxOutcome
):
  lazy val netOutcome: TaxOutcome = fedOutcome + stateOutcome
