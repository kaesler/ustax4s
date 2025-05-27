package org.kae.ustax4s.federal

import cats.{PartialOrder, Show}
import org.kae.ustax4s.SourceLoc
import org.kae.ustax4s.money.{IncomeThreshold, TaxPayable, TaxableIncome}
import scala.math.Ordering.Implicits.infixOrderingOps

// Note: contain a Brackets[FederalTaxRate] rather than opaque type
// so we can precompute and store val properties.
final case class OrdinaryBrackets(
  brackets: Brackets[FederalTaxRate]
):
  require(brackets.isProgressive, SourceLoc())
  require(brackets.contains(IncomeThreshold.zero), SourceLoc())

  lazy val bracketsAscending: Vector[(IncomeThreshold, FederalTaxRate)] =
    brackets.bracketsAscending

  private lazy val thresholdsDescending = bracketsAscending.reverse

  // Adjust the bracket starts for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): OrdinaryBrackets =
    OrdinaryBrackets(brackets.inflatedBy(factor))

  lazy val thresholds: Set[IncomeThreshold] = brackets.thresholds
  lazy val rates: Set[FederalTaxRate]       = brackets.rates

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
    require(bracketExists(bracketRate), SourceLoc())

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

  lazy val ratesForBoundedBrackets: Vector[FederalTaxRate] =
    thresholdsDescending
      .drop(1)
      .reverse
      .map(_._2)

  private lazy val bracketWidths: Map[FederalTaxRate, TaxableIncome] =
    bracketsAscending
      .zip(bracketsAscending.tail)
      .map: (bracket, successorBracket) =>
        val (lowerThreshold, rate) = bracket
        val upperThreshold         = successorBracket._1
        (rate, upperThreshold.absoluteDifference(lowerThreshold))
      .toMap

  // TODO: Unit test
  def bracketWidth(bracketRate: FederalTaxRate): Option[TaxableIncome] =
    bracketWidths.get(bracketRate)

  private def bracketExists(bracketRate: FederalTaxRate): Boolean =
    bracketsAscending.exists(_._2 == bracketRate)

end OrdinaryBrackets

object OrdinaryBrackets:

  given Show[OrdinaryBrackets]:
    def show(b: OrdinaryBrackets): String =
      b.bracketsAscending.mkString("\n")

  given PartialOrder[OrdinaryBrackets] = PartialOrder.by(_.brackets)

  def of(pairs: Iterable[(Int, Double)]): OrdinaryBrackets =
    OrdinaryBrackets(
      Brackets.of(
        pairs.map { (bracketStart, ratePercentage) =>
          require(ratePercentage < 100.0d, SourceLoc())
          IncomeThreshold(bracketStart) ->
            FederalTaxRate.unsafeFrom(ratePercentage / 100.0d)
        }
      )
    )
