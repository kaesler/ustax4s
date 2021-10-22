package org.kae.ustax4s.federal.regimes

import cats.implicits.*
import java.time.{LocalDate, Month, Year}
import org.kae.ustax4s
import org.kae.ustax4s.federal.OrdinaryIncomeBrackets
import org.kae.ustax4s.federal.QualifiedIncomeBrackets
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}
import scala.annotation.tailrec
import scala.math.Ordered

sealed trait Regime extends (Year => RegimeYear) with Product:

  def ordinaryIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): OrdinaryIncomeBrackets

  def qualifiedIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): QualifiedIncomeBrackets

  def standardDeduction(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate
  ): Money

  def personalExemptionDeduction(year: Year, personalExemptions: Int): Money

end Regime

object Regime:
  val InflationAssumed             = 0.02
  val FirstYearTrumpRegimeRequired = 2018
  val LastYearTrumpRegimeRequired  = 2025
  val YearsTrumpTaxRegimeRequired: Set[Year] =
    (FirstYearTrumpRegimeRequired to LastYearTrumpRegimeRequired)
      .map(Year.of)
      .toSet

  def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    birthDate.isBefore(
      LocalDate
        .of(taxYear.getValue, Month.JANUARY.getValue, 2)
        .minusYears(65)
    )

  final case class RegimeInvalidForYear(
    regime: Regime,
    year: Year
  ) extends RuntimeException(
        s"Regime ${regime.productPrefix} cannot apply in ${year.toString}"
      )

end Regime

case object Trump extends Regime:
  import Regime.*

  override def apply(year: Year): RegimeYear =
    failIfInvalid(year)
    RegimeYear(this, year)

  @tailrec
  override def ordinaryIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): OrdinaryIncomeBrackets =
    failIfInvalid(year)
    (year.getValue, filingStatus) match

      // Note: for now assume 2021 rates in later years.
      // TODO: should I inflate them instead ? Closer to what
      // MaxiFi would do?
      case (year, fs) if year > 2021 =>
        ordinaryIncomeBrackets(Year.of(2021), fs)

      case (2021, HeadOfHousehold) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            14200  -> 12,
            54200  -> 22,
            86350  -> 24,
            164900 -> 32,
            209400 -> 35,
            523600 -> 37
          ).view.mapValues(_.toDouble).toMap
        )

      case (2021, Single) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            9950   -> 12,
            40525  -> 22,
            86375  -> 24,
            164925 -> 32,
            209425 -> 35,
            523600 -> 37
          ).view.mapValues(_.toDouble).toMap
        )

      case (2020, HeadOfHousehold) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            14100  -> 12,
            53700  -> 22,
            85500  -> 24,
            163300 -> 32,
            207350 -> 35,
            518400 -> 37
          ).view.mapValues(_.toDouble).toMap
        )

      case (2019, HeadOfHousehold) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            13850  -> 12,
            52850  -> 22,
            84200  -> 24,
            160700 -> 32,
            204100 -> 35,
            510300 -> 37
          ).view.mapValues(_.toDouble).toMap
        )

      case (2018, HeadOfHousehold) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            13600  -> 12,
            51800  -> 22,
            82500  -> 24,
            157500 -> 32,
            200000 -> 35,
            500000 -> 37
          ).view.mapValues(_.toDouble).toMap
        )

      case _ => throw NotYetImplemented(year)

    end match

  override def qualifiedIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): QualifiedIncomeBrackets = QualifiedIncomeBrackets.of(year, filingStatus)

  override def personalExemptionDeduction(
    year: Year,
    personalExemptions: Int
  ): Money = 0

  override def standardDeduction(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate
  ): Money =
    failIfInvalid(year)
    stdDeductionUnadjustedForAge(year, filingStatus) +
      // TODO: should the 1350 be inflated, if we go that way?
      (if isAge65OrOlder(birthDate, year) then 1350 else 0)

  @tailrec
  private def stdDeductionUnadjustedForAge(year: Year, filingStatus: FilingStatus): Money =
    (year.getValue, filingStatus) match

      // Note: for now assume 2021 rates in later years
      // TODO: Should I just inflate by 2% per year?
      case (year, fs) if year > 2021 =>
        stdDeductionUnadjustedForAge(Year.of(2021), fs)

      case (2021, HeadOfHousehold) => 18800
      case (2020, HeadOfHousehold) => 18650
      case (2019, HeadOfHousehold) => 18350
      case (2018, HeadOfHousehold) => 18000

      case (2021, Single) => 12550
      case (2020, Single) => 12400
      case (2019, Single) => 12200
      case (2018, Single) => 12000

      case _ => throw ustax4s.NotYetImplemented(year)

  private def failIfInvalid(year: Year): Unit =
    // Note: Trump regime may be extended beyond 2025 by legislation.
    if year.getValue < FirstYearTrumpRegimeRequired then throw RegimeInvalidForYear(this, year)
    else ()

case object NonTrump extends Regime:
  import Regime.*

  override def apply(year: Year): RegimeYear =
    failIfInvalid(year)
    RegimeYear(this, year)

  override def ordinaryIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): OrdinaryIncomeBrackets =
    failIfInvalid(year)

    (year.getValue, filingStatus) match
      // An estimate for 2021 as if the Trump tax cuts had not occurred.
      // We assume the Trump tax cuts will lapse effective 2026,
      // so we calculate the rates as of 2021 as if they had not happened,
      // assuming the brackets inflated 2% per year from 2017 to 2021.
      // An estimate of course.
      // https://en.wikipedia.org/wiki/Tax_Cuts_and_Jobs_Act_of_2017
      // TODO: inflate to "year" rather than to 2021 ?
      case (year, fs) if year > 2025 =>
        ordinaryIncomeBrackets(Year.of(2017), fs).inflatedBy(
          math.pow(1.0 + InflationAssumed, (2021 - 2017).toDouble)
        )

      case (2017, Single) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10d,
            9235   -> 15d,
            37950  -> 25d,
            91900  -> 28d,
            191650 -> 33d,
            416700 -> 35d,
            418400 -> 39.6d
          )
        )

      case (2017, HeadOfHousehold) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10d,
            13350  -> 15d,
            50800  -> 25d,
            131200 -> 28d,
            212500 -> 33d,
            416700 -> 35d,
            444550 -> 39.6d
          )
        )

      case (2016, Single) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10d,
            9275   -> 15d,
            37650  -> 25d,
            91150  -> 28d,
            190150 -> 33d,
            413350 -> 35d,
            415050 -> 39.6d
          )
        )

      case (2016, HeadOfHousehold) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10d,
            13250  -> 15d,
            50400  -> 25d,
            130150 -> 28d,
            210800 -> 33d,
            413350 -> 35d,
            441000 -> 39.6d
          )
        )

      case _ => throw ustax4s.NotYetImplemented(year)
    end match

  override def qualifiedIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): QualifiedIncomeBrackets = QualifiedIncomeBrackets.of(year, filingStatus)

  override def standardDeduction(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate
  ): Money = {
    failIfInvalid(year)
    // TODO: Did this adjustment apply pre-Trump?
    stdDeductionUnadjustedForAge(year, filingStatus) +
      (if isAge65OrOlder(birthDate, year) then 1350 else 0)
  }

  override def personalExemptionDeduction(
    year: Year,
    personalExemptions: Int
  ): Money = personalExemption(year) mul personalExemptions

  private def personalExemption(year: Year): Money = year.getValue match
    // TODO: Index for inflation?
    case 2017 => 4050
    case 2016 => 4050
    case _    => 4050

  private def stdDeductionUnadjustedForAge(year: Year, filingStatus: FilingStatus): Money =
    (year.getValue, filingStatus) match

      case (year, fs) if year > LastYearTrumpRegimeRequired =>
        stdDeductionUnadjustedForAge(Year.of(2017), fs)
          .mul(
            math.pow(1.0 + InflationAssumed, (2021 - 2017).toDouble)
          )
          .rounded

      case (2017, HeadOfHousehold) => 9350
      case (2017, Single)          => 6350

      case (2016, HeadOfHousehold) => 9300
      case (2016, Single)          => 6300

      case _ => throw ustax4s.NotYetImplemented(year)

    end match

  private def failIfInvalid(year: Year): Unit =
    if YearsTrumpTaxRegimeRequired(year) then throw RegimeInvalidForYear(this, year)
