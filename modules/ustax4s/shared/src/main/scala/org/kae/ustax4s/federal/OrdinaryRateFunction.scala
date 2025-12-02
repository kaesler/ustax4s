package org.kae.ustax4s.federal

import cats.{PartialOrder, Show}
import org.kae.ustax4s.{Bracket, RateFunction, SourceLoc}
import org.kae.ustax4s.money.{IncomeThreshold, TaxPayable, TaxableIncome}
import scala.math.Ordering.Implicits.infixOrderingOps

// Note: contain a RateFunction[FederalTaxRate] rather than opaque type
// so we can precompute and store val properties.
final case class OrdinaryRateFunction(
  rateFunction: RateFunction[FederalTaxRate]
):
  import OrdinaryRateFunction.*

  require(rateFunction.isProgressive, SourceLoc())
  require(rateFunction.has(IncomeThreshold.zero), SourceLoc())

  lazy val bracketsAscending: Vector[Bracket[FederalTaxRate]] =
    rateFunction.bracketsAscending

  private lazy val thresholdsDescending = bracketsAscending.reverse

  // Adjust the bracket starts for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): OrdinaryRateFunction =
    OrdinaryRateFunction(rateFunction.inflatedBy(factor))

  lazy val thresholds: Set[IncomeThreshold] = rateFunction.thresholds
  lazy val rates: Set[FederalTaxRate]       = rateFunction.rates

  def unsafeTaxableIncomeToEndOfBracket(bracketRate: FederalTaxRate): TaxableIncome =
    bracketsAscending
      .sliding(2)
      .collect:
        case Vector((_, `bracketRate`), (nextThreshold, _)) =>
          nextThreshold
      .toList
      .headOption
      .getOrElse(throw errorForBadRate(bracketRate))
  end unsafeTaxableIncomeToEndOfBracket

  def unsafeTaxToEndOfBracket(bracketRate: FederalTaxRate): TaxPayable =
    val taxes = bracketsAscending
      .sliding(2)
      .toList
      .takeWhile:
        case Vector((_, rate), (_, _)) => rate <= bracketRate
        case _                         => throw NoSuchFederalTaxRate(bracketRate)
      .collect:
        case Vector((threshold, rate), (nextThreshold, _)) =>
          // Tax due on current bracket:
          (nextThreshold absoluteDifference threshold).taxAt(rate)
    taxes.foldLeft(TaxPayable.zero)(_ + _)
  end unsafeTaxToEndOfBracket

  lazy val ratesForBoundedBrackets: Vector[FederalTaxRate] =
    thresholdsDescending
      .drop(1)
      .reverse
      .map(_.rate)
  end ratesForBoundedBrackets

  private lazy val bracketWidths: Map[FederalTaxRate, TaxableIncome] =
    bracketsAscending
      .zip(bracketsAscending.tail)
      .map: (bracket, successorBracket) =>
        val (lowerThreshold, rate) = bracket
        val upperThreshold         = successorBracket.threshold
        (rate, upperThreshold.absoluteDifference(lowerThreshold))
      .toMap
  end bracketWidths

  def unsafeBracketWidth(bracketRate: FederalTaxRate): TaxableIncome =
    bracketWidth(bracketRate).getOrElse(
      throw errorForBadRate(bracketRate)
    )
  end unsafeBracketWidth

  def bracketWidth(bracketRate: FederalTaxRate): Option[TaxableIncome] =
    bracketWidths.get(bracketRate)

  private def errorForBadRate(bracketRate: FederalTaxRate) =
    if !bracketExists(bracketRate) then NoSuchFederalTaxRate(bracketRate)
    else if bracketIsTop(bracketRate) then BracketHasNoEnd(bracketRate)
    else RuntimeException(s"Unknown error for FederalTaxRate $bracketRate")
  end errorForBadRate

  private def bracketExists(bracketRate: FederalTaxRate): Boolean =
    bracketsAscending.exists(_.rate == bracketRate)

  private def bracketIsTop(bracketRate: FederalTaxRate): Boolean =
    bracketRate == bracketsAscending.last.rate

end OrdinaryRateFunction

object OrdinaryRateFunction:

  final case class NoSuchFederalTaxRate(
    federalTaxRate: FederalTaxRate
  ) extends RuntimeException(
        s"No such ordinary income Federal tax rate: $federalTaxRate"
      )

  final case class BracketHasNoEnd(
    federalTaxRate: FederalTaxRate
  ) extends RuntimeException(
        s"Ordinary income bracket for $federalTaxRate has no end"
      )

  given Show[OrdinaryRateFunction]:
    def show(b: OrdinaryRateFunction): String =
      b.bracketsAscending.mkString("\n")

  given PartialOrder[OrdinaryRateFunction] = PartialOrder.by(_.rateFunction)

  def of(pairs: Iterable[(Int, Double)]): OrdinaryRateFunction =
    OrdinaryRateFunction(
      RateFunction.of(
        pairs.map: (bracketStart, ratePercentage) =>
          require(ratePercentage < 100.0d, SourceLoc())
          IncomeThreshold(bracketStart) ->
            FederalTaxRate.unsafeFrom(ratePercentage / 100.0d)
      )
    )
  end of
end OrdinaryRateFunction
