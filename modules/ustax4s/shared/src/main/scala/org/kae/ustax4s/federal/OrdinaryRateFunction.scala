package org.kae.ustax4s.federal

import cats.{PartialOrder, Show}
import org.kae.ustax4s.{Bracket, RateFunction, SourceLoc}
import org.kae.ustax4s.money.{IncomeThreshold, TaxPayable, TaxableIncome}
import scala.math.Ordering.Implicits.infixOrderingOps

// Note: contain a RateFunction[FederalTaxRate] rather than opaque type
// so we can precompute and store val properties.
final case class OrdinaryRateFunction(
  rateFunction: RateFunction[FedTaxRate]
):
  import OrdinaryRateFunction.*

  require(rateFunction.isProgressive, SourceLoc())
  require(rateFunction.has(IncomeThreshold.zero), SourceLoc())

  lazy val bracketsAscending: Vector[Bracket[FedTaxRate]] =
    rateFunction.bracketsAscending

  private lazy val thresholdsDescending = bracketsAscending.reverse

  // Adjust the bracket starts for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): OrdinaryRateFunction =
    OrdinaryRateFunction(rateFunction.inflatedBy(factor))

  lazy val thresholds: Set[IncomeThreshold] = rateFunction.thresholds
  lazy val rates: Set[FedTaxRate]       = rateFunction.rates

  def unsafeTaxableIncomeToEndOfBracket(bracketRate: FedTaxRate): TaxableIncome =
    bracketsAscending
      .sliding(2)
      .collect:
        case Vector((_, `bracketRate`), (nextThreshold, _)) =>
          nextThreshold
      .toList
      .headOption
      .getOrElse(throw errorForBadRate(bracketRate))
  end unsafeTaxableIncomeToEndOfBracket

  def unsafeTaxToEndOfBracket(bracketRate: FedTaxRate): TaxPayable =
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

  lazy val ratesForBoundedBrackets: Vector[FedTaxRate] =
    thresholdsDescending
      .drop(1)
      .reverse
      .map(_.rate)
  end ratesForBoundedBrackets

  private lazy val bracketWidths: Map[FedTaxRate, TaxableIncome] =
    bracketsAscending
      .zip(bracketsAscending.tail)
      .map: (bracket, successorBracket) =>
        val (lowerThreshold, rate) = bracket
        val upperThreshold         = successorBracket.threshold
        (rate, upperThreshold.absoluteDifference(lowerThreshold))
      .toMap
  end bracketWidths

  def unsafeBracketWidth(bracketRate: FedTaxRate): TaxableIncome =
    bracketWidth(bracketRate).getOrElse(
      throw errorForBadRate(bracketRate)
    )
  end unsafeBracketWidth

  def bracketWidth(bracketRate: FedTaxRate): Option[TaxableIncome] =
    bracketWidths.get(bracketRate)

  private def errorForBadRate(bracketRate: FedTaxRate) =
    if !bracketExists(bracketRate) then NoSuchFederalTaxRate(bracketRate)
    else if bracketIsTop(bracketRate) then BracketHasNoEnd(bracketRate)
    else RuntimeException(s"Unknown error for FederalTaxRate $bracketRate")
  end errorForBadRate

  private def bracketExists(bracketRate: FedTaxRate): Boolean =
    bracketsAscending.exists(_.rate == bracketRate)

  private def bracketIsTop(bracketRate: FedTaxRate): Boolean =
    bracketRate == bracketsAscending.last.rate

end OrdinaryRateFunction

object OrdinaryRateFunction:

  final case class NoSuchFederalTaxRate(
    federalTaxRate: FedTaxRate
  ) extends RuntimeException(
        s"No such ordinary income Federal tax rate: $federalTaxRate"
      )

  final case class BracketHasNoEnd(
    federalTaxRate: FedTaxRate
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
            FedTaxRate.unsafeFrom(ratePercentage / 100.0d)
      )
    )
  end of
end OrdinaryRateFunction
