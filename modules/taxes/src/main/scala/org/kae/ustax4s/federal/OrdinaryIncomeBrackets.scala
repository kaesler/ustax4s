package org.kae.ustax4s.federal

import java.time.Year
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import scala.annotation.tailrec
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}

/** Calculates tax on ordinary (non-investment) income.
  *
  * @param bracketStarts
  *   the tax brackets in effect
  */
final case class OrdinaryIncomeBrackets(
  bracketStarts: Map[Money, FederalTaxRate]
):
  require(bracketStarts.contains(Money.zero))

  // Adjust the bracket starts for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): OrdinaryIncomeBrackets =
    require(factor >= 1.0)
    OrdinaryIncomeBrackets(
      bracketStarts.map { (start, rate) =>
        ((start mul factor).rounded, rate)
      }
    )

  val bracketStartsAscending: Vector[(Money, FederalTaxRate)] =
    bracketStarts.toVector.sortBy(_._1)

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  def taxDueWholeDollar(taxableOrdinaryIncome: Money): Money =
    taxDue(taxableOrdinaryIncome).rounded

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

  // Note: kept here for translation to TypeScript.
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

  def endOfLowestBracket: Money =
    bracketStarts.keySet.toList.sorted.apply(1)

  def startOfTopBracket: Money = bracketStarts.keySet.toList.max

object OrdinaryIncomeBrackets:

  def create(pairs: Map[Int, Double]): OrdinaryIncomeBrackets =
    OrdinaryIncomeBrackets(
      pairs.map { (bracketStart, ratePercentage) =>
        require(ratePercentage < 100.0d)
        Money(bracketStart) ->
          FederalTaxRate.unsafeFrom(ratePercentage / 100.0d)
      }
    )
