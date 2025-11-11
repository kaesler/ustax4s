package org.kae.ustax4s.states

import org.kae.ustax4s.Brackets.Brackets

sealed trait StateRegime

case object NilStateRegime extends StateRegime

case class FlatStateRegime(
  rate: StateTaxRate
) extends StateRegime

case class ProgressStateRegime(
  brackets: Brackets[StateTaxRate]
) extends StateRegime
