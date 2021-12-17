package org.kae.ustax4s.federal

import cats.Show
import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.{Income, IncomeThreshold, TaxPayable}
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}
import scala.annotation.tailrec

/** Calculates tax on ordinary (non-investment) income.
  *
  * @param bracketStarts
  *   the tax brackets in effect
  */
final case class OrdinaryIncomeBrackets(
  bracketStarts: Map[IncomeThreshold, FederalTaxRate]
):
  require(bracketStarts.contains(IncomeThreshold.zero))

  // Adjust the bracket starts for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): OrdinaryIncomeBrackets =
    require(factor >= 1.0)
    OrdinaryIncomeBrackets(
      bracketStarts.map { (start, rate) =>
        (start.increaseBy(factor).rounded, rate)
      }
    )

  val bracketStartsAscending: Vector[(IncomeThreshold, FederalTaxRate)] =
    bracketStarts.toVector.sortBy(_._1)

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  def taxDueWholeDollar(taxableOrdinaryIncome: Income): TaxPayable =
    taxDue(taxableOrdinaryIncome).rounded

  def taxDue(taxableOrdinaryIncome: Income): TaxPayable =

    // Note: Qualified investment income sort of occupies the top brackets above
    // ordinary income and so does not affect this.

    case class Accum(ordinaryIncomeYetToBeTaxed: Income, taxSoFar: TaxPayable)
    object Accum:
      def initial: Accum = apply(taxableOrdinaryIncome, TaxPayable.zero)

    val accum = bracketsStartsDescending.foldLeft(Accum.initial) {

      case (
            Accum(ordinaryIncomeYetToBeTaxed, taxSoFar),
            (bracketStart, bracketRate)
          ) =>
        // Result will be non-negative: so becomes zero if bracket does not apply.
        val ordinaryIncomeInThisBracket =
          ordinaryIncomeYetToBeTaxed.amountAbove(bracketStart)

        // Non-negative: so zero if bracket does not apply.
        val taxInThisBracket = ordinaryIncomeInThisBracket taxAt bracketRate
        Accum(
          ordinaryIncomeYetToBeTaxed =
            ordinaryIncomeYetToBeTaxed reduceBy ordinaryIncomeInThisBracket,
          taxSoFar = taxSoFar + taxInThisBracket
        )
    }
    assert(accum.ordinaryIncomeYetToBeTaxed.isZero)
    val res = accum.taxSoFar
    // println(s"taxDueFunctionally($taxableOrdinaryIncome): $res")
    res

  def taxableIncomeToEndOfBracket(bracketRate: FederalTaxRate): Income =
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
      .asIncome

  def taxToEndOfBracket(bracketRate: FederalTaxRate): TaxPayable =
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
        (nextBracketStart absoluteDifference bracketStart).taxAt(rate)
      }

    taxes.foldLeft(TaxPayable.zero)(_ + _)

  def bracketWidth(bracketRate: FederalTaxRate): Income =
    bracketStartsAscending
      .sliding(2)
      .collect { case Vector((rateStart, `bracketRate`), (nextRateStart, _)) =>
        nextRateStart absoluteDifference rateStart
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

  def endOfLowestBracket: IncomeThreshold =
    bracketStarts.keySet.toList.sorted.apply(1)

  def startOfTopBracket: IncomeThreshold = bracketStarts.keySet.toList.max

object OrdinaryIncomeBrackets:

  given Show[OrdinaryIncomeBrackets] with
    def show(b: OrdinaryIncomeBrackets): String =
      b.bracketStartsAscending.mkString("\n")

  def create(pairs: Map[Int, Double]): OrdinaryIncomeBrackets =
    OrdinaryIncomeBrackets(
      pairs.map { (bracketStart, ratePercentage) =>
        require(ratePercentage < 100.0d)
        IncomeThreshold(bracketStart) ->
          FederalTaxRate.unsafeFrom(ratePercentage / 100.0d)
      }
    )
