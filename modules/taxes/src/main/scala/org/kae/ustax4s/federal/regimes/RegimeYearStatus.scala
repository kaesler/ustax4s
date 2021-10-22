package org.kae.ustax4s.federal.regimes

import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.{OrdinaryIncomeBrackets, QualifiedIncomeBrackets}

final case class RegimeYearStatus(
  regimeYear: RegimeYear,
  filingStatus: FilingStatus,
  // Note: this is the hook for inflating the brackets
  inflationFactor: Double = 1.0
) extends (Person => RegimeYearStatusPerson):

  override def apply(person: Person): RegimeYearStatusPerson =
    RegimeYearStatusPerson(this, person)

  def ordinaryIncomeBrackets: OrdinaryIncomeBrackets =
    regimeYear.regime
      .ordinaryIncomeBrackets(year, filingStatus)
      .inflatedBy(inflationFactor)

  def qualifiedIncomeBrackets: QualifiedIncomeBrackets =
    regimeYear.regime
      .qualifiedIncomeBrackets(year, filingStatus)
      .inflatedBy(inflationFactor)

  def withInflationFactor(factor: Double): RegimeYearStatus =
    copy(inflationFactor = factor)

  private inline def year = regimeYear.year

end RegimeYearStatus
