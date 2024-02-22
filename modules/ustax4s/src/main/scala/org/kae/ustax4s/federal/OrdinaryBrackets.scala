package org.kae.ustax4s.federal

import cats.implicits.*
import cats.{PartialOrder, Show}
import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.{Income, IncomeThreshold, TaxPayable, TaxableIncome}
import org.kae.ustax4s.taxfunction.TaxFunction
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}
import scala.annotation.tailrec
import scala.math.Ordering.Implicits.infixOrderingOps

// Note: contain a Brackets[FederalTaxRate] rather than opaque type
// so we can precompute and store val properties.
final case class OrdinaryBrackets(
  brackets: Brackets[FederalTaxRate]
):
  require(brackets.isProgressive)
  require(brackets.contains(IncomeThreshold.zero))

  val bracketsAscending: Vector[(IncomeThreshold, FederalTaxRate)] =
    brackets.bracketsAscending

  private val thresholdsDescending = bracketsAscending.reverse

  // Adjust the bracket starts for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): OrdinaryBrackets =
    OrdinaryBrackets(brackets.inflatedBy(factor))

  def thresholds: Set[IncomeThreshold] = brackets.thresholds
  def rates: Set[FederalTaxRate]       = brackets.rates

  def taxableIncomeToEndOfBracket(bracketRate: FederalTaxRate): TaxableIncome =
    bracketsAscending
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

  def taxToEndOfBracket(bracketRate: FederalTaxRate): TaxPayable =
    require(bracketExists(bracketRate))

    val taxes = bracketsAscending
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

  private def bracketExists(bracketRate: FederalTaxRate): Boolean =
    bracketsAscending.exists { (_, rate) => rate == bracketRate }

end OrdinaryBrackets

object OrdinaryBrackets:

  given Show[OrdinaryBrackets] with
    def show(b: OrdinaryBrackets): String =
      b.bracketsAscending.mkString("\n")

  given [R]: PartialOrder[OrdinaryBrackets] = PartialOrder.by(_.brackets)

  def of(pairs: Iterable[(Int, Double)]): OrdinaryBrackets =
    OrdinaryBrackets(
      Brackets.of(
        pairs.map { (bracketStart, ratePercentage) =>
          require(ratePercentage < 100.0d)
          IncomeThreshold(bracketStart) ->
            FederalTaxRate.unsafeFrom(ratePercentage / 100.0d)
        }
      )
    )
