package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.{LocalDate, Month, Year}
import org.kae.ustax4s
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.*
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}
import scala.util.chaining.*

sealed trait Regime:
  def name: String
  def lastYearKnown: Year
  def failIfInvalid(year: Year): Unit
end Regime

object Regime:
  val values: Set[Regime] = Set(PreTrump, Trump)

  def parse(s: String): Option[Regime] = values.find(_.name == s)
  def unsafeParse(s: String): Regime = parse(s).getOrElse(
    throw new RuntimeException(s"No such regime: $s")
  )

  val FirstYearTrumpRegimeRequired = 2018
  val LastYearTrumpRegimeRequired  = 2025
  val YearsTrumpTaxRegimeRequired: Set[Year] =
    (FirstYearTrumpRegimeRequired to LastYearTrumpRegimeRequired)
      .map(Year.of)
      .toSet

  final case class RegimeInvalidForYear(
    regime: Regime,
    year: Year
  ) extends RuntimeException(
        s"Regime ${regime.name} cannot apply in ${year.toString}"
      )

end Regime

case object Trump extends Regime:
  import Regime.*

  override val name: String = productPrefix

  override val lastYearKnown: Year = Year.of(2022)

  override def failIfInvalid(year: Year): Unit =
    // Note: Trump regime may be extended beyond 2025 by legislation.
    if year.getValue < FirstYearTrumpRegimeRequired then throw RegimeInvalidForYear(this, year)
    else ()

end Trump

case object PreTrump extends Regime:
  import Regime.*

  override val name: String = this.productPrefix

  override val lastYearKnown: Year = Year.of(2017)

  override def failIfInvalid(year: Year): Unit =
    if YearsTrumpTaxRegimeRequired(year) then throw RegimeInvalidForYear(this, year)

end PreTrump
