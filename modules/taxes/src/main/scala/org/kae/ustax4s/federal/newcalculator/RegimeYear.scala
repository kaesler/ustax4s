package org.kae.ustax4s.federal.newcalculator

import java.time.Year
import org.kae.ustax4s.FilingStatus

final case class RegimeYear(
  regime: Regime,
  year: Year
) extends (FilingStatus => RegimeYearStatus):
  override def apply(filingStatus: FilingStatus): RegimeYearStatus =
    RegimeYearStatus(this, filingStatus)
