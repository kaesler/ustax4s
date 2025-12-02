package org.kae.ustax4s.federal

import cats.{PartialOrder, Show}
import org.kae.ustax4s.{Bracket, RateFunction, SourceLoc}
import org.kae.ustax4s.money.IncomeThreshold

// Note: contain a RateFunction[FederalTaxRate] rather than opaque type
// so we can precompute and store val properties.
final case class QualifiedRateFunction(
  function: RateFunction[FederalTaxRate]
):
  require(function.isProgressive, function.toString + ", at " + SourceLoc())
  require(function.has(IncomeThreshold.zero), SourceLoc())
  require(function.size >= 2, SourceLoc())

  val bracketsAscending: Vector[Bracket[FederalTaxRate]] =
    function.bracketsAscending
  require(
    bracketsAscending(0) == (IncomeThreshold.zero, FederalTaxRate.unsafeFrom(0.0)),
    SourceLoc()
  )

  // Adjust the thresholds for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): QualifiedRateFunction =
    QualifiedRateFunction(function.inflatedBy(factor))

  lazy val startOfNonZeroQualifiedRateBracket: IncomeThreshold =
    bracketsAscending(1).threshold

end QualifiedRateFunction

object QualifiedRateFunction:

  def of(pairs: Iterable[(Int, Int)]): QualifiedRateFunction =
    apply(
      RateFunction.of(
        pairs.map: (bracketStart, ratePercentage) =>
          require(ratePercentage < 100, SourceLoc())
          IncomeThreshold(bracketStart) ->
            FederalTaxRate.unsafeFrom(ratePercentage.toDouble / 100.0d)
      )
    )
  end of

  given Show[QualifiedRateFunction]:
    def show(b: QualifiedRateFunction): String =
      b.bracketsAscending.mkString("\n")

  given PartialOrder[QualifiedRateFunction] = PartialOrder.by(_.function)

end QualifiedRateFunction
