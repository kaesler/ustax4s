package org.kae.ustax4s.federal

import cats.Show
import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.{Income, IncomeThreshold, TaxPayable}
import org.kae.ustax4s.tax.Tax
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}
import scala.annotation.tailrec

// TODO: Do I need all this code now.
/** Calculates tax on ordinary (non-investment) income.
  *
  * @param bracketStarts
  *   the tax brackets in effect
  */
final case class OrdinaryIncomeBrackets(
  bracketStarts: Map[IncomeThreshold, FederalTaxRate]
):
  // Note: well-formedness checks.
  require(isProgressive)
  require(bracketStarts.contains(IncomeThreshold.zero))
  require(bracketStarts.nonEmpty)

  // TODO: maybe move to Tax
  def asRateDeltas: List[(IncomeThreshold, FederalTaxRate)] =
    bracketStarts.keys.toList.sorted.zip(rateDeltas)

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

  def ratesForBoundedBrackets: Vector[FederalTaxRate] =
    bracketsStartsDescending
      .drop(1)
      .reverse
      .map(_._2)

  private def isProgressive: Boolean =
    val ratesAscending = bracketStarts.toList.sorted.map(_._2)
    ratesAscending.zip(ratesAscending.tail).forall { (left, right) =>
      left < right
    }

  private def bracketExists(bracketRate: FederalTaxRate): Boolean =
    bracketStartsAscending.exists { (_, rate) => rate == bracketRate }

  // TODO: maybe move to Tax
  private def rateDeltas: List[FederalTaxRate] =
    val ratesWithZeroAtFront =
      FederalTaxRate.zero :: bracketStarts.values.toList.sorted
    ratesWithZeroAtFront
      .zip(ratesWithZeroAtFront.tail)
      .map { (previousRate, currentRate) =>
        currentRate absoluteDifference previousRate
      }

end OrdinaryIncomeBrackets

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
