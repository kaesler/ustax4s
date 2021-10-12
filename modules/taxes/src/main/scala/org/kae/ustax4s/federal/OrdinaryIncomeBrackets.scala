package org.kae.ustax4s.federal

import java.time.Year
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import scala.annotation.tailrec
import org.kae.ustax4s.FilingStatus

/** Calculates tax on ordinary (non-investment) income.
  *
  * @param bracketStarts
  *   the tax brackets in effect
  */
final case class OrdinaryIncomeBrackets(
  bracketStarts: Map[Money, FederalTaxRate]
):
  require(bracketStarts.contains(Money.zero))

  val bracketStartsAscending: Vector[(Money, FederalTaxRate)] =
    bracketStarts.toVector.sortBy(_._1)

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  /** @return
    *   the tax due on the taxable ordinary income (not LTCGs...)
    * @param taxableOrdinaryIncome
    *   the ordinary income
    */
  def taxDue(taxableOrdinaryIncome: Money): Money =
    taxDueFunctionally(taxableOrdinaryIncome)

  private def taxDueFunctionally(taxableOrdinaryIncome: Money): Money =

    // Note: Qualified investment income sort of occupies the top brackets above
    // ordinary income and so does not affect this.

    case class Accum(ordinaryIncomeYetToBeTaxed: Money, taxSoFar: Money)
    object Accum:
      def initial: Accum = apply(taxableOrdinaryIncome, Money.zero)

    val accum = bracketsStartsDescending.foldLeft(Accum.initial) {

      case (
            Accum(ordinaryIncomeYetToBeTaxed, taxSoFar),
            (bracketStart, bracketRate)
          ) =>
        // Result will be non-negative: so becomes zero if bracket does not apply.
        val ordinaryIncomeInThisBracket =
          ordinaryIncomeYetToBeTaxed subp bracketStart

        // Non-negative: so zero if bracket does not apply.
        val taxInThisBracket = ordinaryIncomeInThisBracket taxAt bracketRate
        Accum(
          ordinaryIncomeYetToBeTaxed = ordinaryIncomeYetToBeTaxed subp ordinaryIncomeInThisBracket,
          taxSoFar = taxSoFar + taxInThisBracket
        )
    }
    assert(accum.ordinaryIncomeYetToBeTaxed.isZero)
    val res = accum.taxSoFar
    // println(s"taxDueFunctionally($taxableOrdinaryIncome): $res")
    res

  def taxDueImperatively(
    taxableOrdinaryIncome: Money
  ): Money =
    var ordinaryIncomeYetToBeTaxed = taxableOrdinaryIncome
    var taxSoFar                   = Money.zero
    bracketsStartsDescending.foreach { (bracketStart, bracketRate) =>
      // Result will be non-negative: so becomes zero if bracket does not apply.
      val ordinaryIncomeInThisBracket =
        ordinaryIncomeYetToBeTaxed subp bracketStart

      // Non-negative: so zero if bracket does not apply.
      val taxInThisBracket = ordinaryIncomeInThisBracket taxAt bracketRate

      ordinaryIncomeYetToBeTaxed = ordinaryIncomeYetToBeTaxed subp ordinaryIncomeInThisBracket
      taxSoFar = taxSoFar + taxInThisBracket
    }
    assert(ordinaryIncomeYetToBeTaxed.isZero)
    taxSoFar

  def taxableIncomeToEndOfBracket(bracketRate: FederalTaxRate): Money =
    bracketStartsAscending
      .sliding(2)
      .collect { case Vector((_, `bracketRate`), (nextBracketStart, _)) =>
        nextBracketStart
      }
      .toList
      .headOption
      .getOrElse(
        throw RuntimeException(
          s"rate not found or has no successor: $bracketRate"
        )
      )

  def taxToEndOfBracket(bracketRate: FederalTaxRate): Money =
    require(bracketExists(bracketRate))

    val taxes = bracketStartsAscending
      .sliding(2)
      .toList
      .takeWhile {
        case Vector((_, rate), (_, _)) => rate <= bracketRate
        // Note: can't happen.
        case _ => false
      }
      .collect { case Vector((bracketStart, rate), (nextBracketStart, _)) =>
        // Tax due on current bracket:
        (nextBracketStart subp bracketStart) taxAt rate
      }

    taxes.foldLeft(0.asMoney)(_ + _)

  def bracketWidth(bracketRate: FederalTaxRate): Money =
    bracketStartsAscending
      .sliding(2)
      .collect { case Vector((rateStart, `bracketRate`), (nextRateStart, _)) =>
        nextRateStart subp rateStart
      }
      .toList
      .headOption
      .getOrElse(
        throw RuntimeException(
          s"rate not found or has no successor: $bracketRate"
        )
      )

  def ratesForBoundedBrackets: Vector[FederalTaxRate] =
    bracketsStartsDescending
      .drop(1)
      .reverse
      .map(_._2)

  def bracketExists(bracketRate: FederalTaxRate): Boolean =
    bracketStartsAscending.exists { (_, rate) => rate == bracketRate }

object OrdinaryIncomeBrackets:

  @tailrec def of(year: Year, status: FilingStatus): OrdinaryIncomeBrackets =
    (year.getValue, status) match

      // Note: for now assume 2021 rates in later years.
      case (year, fs) if year > 2021 => of(Year.of(2021), fs)

      case (2021, HeadOfHousehold) =>
        create(
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
        create(
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
        create(
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
        create(
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
        create(
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

      case (2017, Single) =>
        create(
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
        create(
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

      case _ => ???
    end match

  private def create(pairs: Map[Int, Double]): OrdinaryIncomeBrackets =
    OrdinaryIncomeBrackets(
      pairs.map { (bracketStart, ratePercentage) =>
        require(ratePercentage < 100.0d)
        Money(bracketStart) ->
          FederalTaxRate.unsafeFrom(ratePercentage / 100.0d)
      }
    )
