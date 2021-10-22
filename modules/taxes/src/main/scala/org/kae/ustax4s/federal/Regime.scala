package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.{LocalDate, Month, Year}
import org.kae.ustax4s
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.{FilingStatus, Inflation, NotYetImplemented}
import scala.annotation.tailrec
import scala.math.Ordered

sealed trait Regime:

  def name: String

  def standardDeduction(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate
  ): Money

  def personalExemptionDeduction(
    year: Year,
    personalExemptions: Int
  ): Money

  def netDeduction(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int,
    itemizedDeductions: Money
  ): Money =
    Money.max(
      standardDeduction(year, filingStatus, birthDate),
      personalExemptionDeduction(year, personalExemptions) + itemizedDeductions
    )

  def ordinaryIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): OrdinaryIncomeBrackets

  def qualifiedIncomeBrackets(
    year: Year,
    filingStatus: FilingStatus
  ): QualifiedIncomeBrackets

  def inflatedBy(inflation: Inflation): Regime =
    val base = this
    new {
      override val name =
        s"${base.name}-inflatedTo-${inflation.targetFutureYear.getValue}"

      override def standardDeduction(
        year: Year,
        filingStatus: FilingStatus,
        birthDate: LocalDate
      ): Money =
        base.standardDeduction(year, filingStatus, birthDate) mul
          inflation.factor(year)

      override def personalExemptionDeduction(
        year: Year,
        personalExemptions: Int
      ): Money =
        base.personalExemptionDeduction(year, personalExemptions) mul
          inflation.factor(year)

      override def ordinaryIncomeBrackets(
        year: Year,
        filingStatus: FilingStatus
      ): OrdinaryIncomeBrackets =
        base
          .ordinaryIncomeBrackets(year, filingStatus)
          .inflatedBy(inflation.factor(year))

      override def qualifiedIncomeBrackets(
        year: Year,
        filingStatus: FilingStatus
      ): QualifiedIncomeBrackets =
        base
          .qualifiedIncomeBrackets(year, filingStatus)
          .inflatedBy(inflation.factor(year))
    }

end Regime

object Regime:
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
        s"Regime ${regime.name} cannot apply in ${year.toString}"
      )

end Regime

case object Trump extends Regime:
  import Regime.*

  override val name = "Trump"

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

  private def failIfInvalid(year: Year): Unit =
    // Note: Trump regime may be extended beyond 2025 by legislation.
    if year.getValue < FirstYearTrumpRegimeRequired then throw RegimeInvalidForYear(this, year)
    else ()

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

case object NonTrump extends Regime {
  import Regime.*

  override val name = "NonTrump"

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

  private def failIfInvalid(year: Year): Unit =
    if YearsTrumpTaxRegimeRequired(year) then throw RegimeInvalidForYear(this, year)

  private def personalExemption(year: Year): Money = year.getValue match
    // TODO: Index for inflation?
    case 2017 => 4050
    case 2016 => 4050
    case _    => 4050

  private def stdDeductionUnadjustedForAge(year: Year, filingStatus: FilingStatus): Money =
    (year.getValue, filingStatus) match

      case (2017, HeadOfHousehold) => 9350
      case (2017, Single)          => 6350

      case (2016, HeadOfHousehold) => 9300
      case (2016, Single)          => 6300

      case _ => throw ustax4s.NotYetImplemented(year)

    end match
}
