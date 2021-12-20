package org.kae.ustax4s.federal

import cats.Show
import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.{Income, IncomeThreshold, TaxPayable}
import org.kae.ustax4s.taxfunction.TaxFunction
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}
import scala.annotation.tailrec

/** Calculates tax on ordinary (non-investment) income.
  *
  * @param thresholds
  *   the tax brackets in effect
  */
final case class OrdinaryIncomeBrackets(
  thresholds: Map[IncomeThreshold, FederalTaxRate]
):
  require(isProgressive)
  require(thresholds.contains(IncomeThreshold.zero))
  require(thresholds.nonEmpty)

  // Adjust the bracket starts for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): OrdinaryIncomeBrackets =
    require(factor >= 1.0)
    OrdinaryIncomeBrackets(
      thresholds.map { (start, rate) =>
        (start.increaseBy(factor).rounded, rate)
      }
    )

  val thresholdsAscending: Vector[(IncomeThreshold, FederalTaxRate)] =
    thresholds.toVector.sortBy(_._1)

  private val thresholdsDescending = thresholdsAscending.reverse

  def taxableIncomeToEndOfBracket(bracketRate: FederalTaxRate): Income =
    thresholdsAscending
      .sliding(2)
      .collect { case Vector((_, `bracketRate`), (nextThreshold, _)) =>
        nextThreshold
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

    val taxes = thresholdsAscending
      .sliding(2)
      .toList
      .takeWhile {
        case Vector((_, rate), (_, _)) => rate <= bracketRate
        // Note: can't happen.
        case _ => false
      }
      .collect { case Vector((threshold, rate), (nextThreshold, _)) =>
        // Tax due on current bracket:
        (nextThreshold absoluteDifference threshold).taxAt(rate)
      }

    taxes.foldLeft(TaxPayable.zero)(_ + _)

  def ratesForBoundedBrackets: Vector[FederalTaxRate] =
    thresholdsDescending
      .drop(1)
      .reverse
      .map(_._2)

  private def isProgressive: Boolean =
    val ratesAscending = thresholds.toList.sorted.map(_._2)
    ratesAscending.zip(ratesAscending.tail).forall { (left, right) =>
      left < right
    }

  private def bracketExists(bracketRate: FederalTaxRate): Boolean =
    thresholdsAscending.exists { (_, rate) => rate == bracketRate }
end OrdinaryIncomeBrackets

object OrdinaryIncomeBrackets:

  given Show[OrdinaryIncomeBrackets] with
    def show(b: OrdinaryIncomeBrackets): String =
      b.thresholdsAscending.mkString("\n")

  def create(pairs: Map[Int, Double]): OrdinaryIncomeBrackets =
    OrdinaryIncomeBrackets(
      pairs.map { (bracketStart, ratePercentage) =>
        require(ratePercentage < 100.0d)
        IncomeThreshold(bracketStart) ->
          FederalTaxRate.unsafeFrom(ratePercentage / 100.0d)
      }
    )
