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

  def unadjustedStandardDeduction(
    year: Year,
    filingStatus: FilingStatus
  ): Deduction
  def adjustmentWhenOver65(year: Year): Deduction
  def adjustmentWhenOver65AndSingle(year: Year): Deduction

  def perPersonExemption(year: Year): Deduction

  def ordinaryIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): OrdinaryIncomeBrackets

  def qualifiedIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): QualifiedIncomeBrackets

  def failIfInvalid(year: Year): Unit

  def bind(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    personalExemptions: Int
  ): BoundRegime =
    BoundRegime.create(this, year, birthDate, filingStatus, personalExemptions)

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

  override def ordinaryIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): OrdinaryIncomeBrackets =
    failIfInvalid(year)
    (year.getValue, filingStatus) match

      // Note: for now assume 2022 rates in later years.
//      case (year, fs) if year > 2022 =>
//        ordinaryIncomeBrackets(Year.of(2022), fs)

      case (2022, HeadOfHousehold) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            14650  -> 12,
            55900  -> 22,
            89050  -> 24,
            170050 -> 32,
            215950 -> 35,
            539900 -> 37
          ).view.mapValues(_.toDouble).toMap
        )

      case (2022, Single) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            10275  -> 12,
            41775  -> 22,
            89075  -> 24,
            170050 -> 32,
            215950 -> 35,
            539900 -> 37
          ).view.mapValues(_.toDouble).toMap
        )

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
      case (2020, Single) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            9875   -> 12,
            40125  -> 22,
            85525  -> 24,
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

      case (2019, Single) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            9700   -> 12,
            39475  -> 22,
            84200  -> 24,
            160725 -> 32,
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

      case (2018, Single) =>
        OrdinaryIncomeBrackets.create(
          Map(
            0      -> 10,
            9525   -> 12,
            38700  -> 22,
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

  override def failIfInvalid(year: Year): Unit =
    // Note: Trump regime may be extended beyond 2025 by legislation.
    if year.getValue < FirstYearTrumpRegimeRequired then throw RegimeInvalidForYear(this, year)
    else ()

  override def perPersonExemption(year: Year): Deduction = Deduction.zero

  override def unadjustedStandardDeduction(year: Year, filingStatus: FilingStatus): Deduction =
    (
      (year.getValue, filingStatus) match

        case (2022, HeadOfHousehold) => 19400
        case (2022, Single)          => 12950

        case (2021, HeadOfHousehold) => 18800
        case (2021, Single)          => 12550

        case (2020, HeadOfHousehold) => 18650
        case (2020, Single)          => 12400

        case (2019, HeadOfHousehold) => 18350
        case (2019, Single)          => 12200

        case (2018, HeadOfHousehold) => 18000
        case (2018, Single)          => 12000

        case _ => throw ustax4s.NotYetImplemented(year)
    ).pipe(Deduction.apply)

  override def adjustmentWhenOver65(year: Year): Deduction =
    (
      year.getValue match
        case 2022 => 1400
        case 2021 => 1350
        case 2020 => 1300
        case 2019 => 1300
        case 2018 => 1300
        case _    => throw ustax4s.NotYetImplemented(year)
    ).pipe(Deduction.apply)

  override def adjustmentWhenOver65AndSingle(year: Year): Deduction =
    (
      year.getValue match
        case 2022 => 350
        case 2021 => 350
        case 2020 => 350
        case 2019 => 350
        case 2018 => 300
        case _    => throw ustax4s.NotYetImplemented(year)
    ).pipe(Deduction.apply)

case object PreTrump extends Regime:
  import Regime.*

  override val name: String = this.productPrefix

  override val lastYearKnown: Year = Year.of(2017)

  override def ordinaryIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): OrdinaryIncomeBrackets =
    failIfInvalid(year)

    (year.getValue, filingStatus) match

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

  override def failIfInvalid(year: Year): Unit =
    if YearsTrumpTaxRegimeRequired(year) then throw RegimeInvalidForYear(this, year)

  override def perPersonExemption(year: Year): Deduction =
    (year.getValue match
      case 2017 => 4050
      case 2016 => 4050
      case _    => throw ustax4s.NotYetImplemented(year)
    ).pipe(Deduction.apply)

  override def unadjustedStandardDeduction(year: Year, filingStatus: FilingStatus): Deduction =
    (
      (year.getValue, filingStatus) match

        case (2017, HeadOfHousehold) => 9350
        case (2017, Single)          => 6350

        case (2016, HeadOfHousehold) => 9300
        case (2016, Single)          => 6300

        case _ => throw ustax4s.NotYetImplemented(year)
    ).pipe(Deduction.apply)

  override def adjustmentWhenOver65(year: Year): Deduction =
    (
      year.getValue match
        case 2017 => 1250
        case 2016 => 1250
        case _    => throw ustax4s.NotYetImplemented(year)
    ).pipe(Deduction.apply)

  override def adjustmentWhenOver65AndSingle(year: Year): Deduction =
    (
      year.getValue match
        case 2017 => 300
        case 2016 => 300
        case _    => throw ustax4s.NotYetImplemented(year)
    ).pipe(Deduction.apply)

end PreTrump
