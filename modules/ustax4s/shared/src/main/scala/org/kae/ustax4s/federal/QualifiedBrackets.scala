package org.kae.ustax4s.federal

import cats.{PartialOrder, Show}
import org.kae.ustax4s.SourceLoc
import org.kae.ustax4s.money.IncomeThreshold

// Note: contain a Brackets[FederalTaxRate] rather than opaque type
// so we can precompute and store val properties.
final case class QualifiedBrackets(
  brackets: Brackets[FederalTaxRate]
):
  require(brackets.isProgressive, brackets.toString + ", at " + SourceLoc())
  require(brackets.contains(IncomeThreshold.zero), SourceLoc())
  require(brackets.size >= 2, SourceLoc())

  val bracketsAscending: Vector[(IncomeThreshold, FederalTaxRate)] =
    brackets.bracketsAscending
  require(
    bracketsAscending(0) == (IncomeThreshold.zero, FederalTaxRate.unsafeFrom(0.0)),
    SourceLoc()
  )

  // Adjust the thresholds for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): QualifiedBrackets =
    QualifiedBrackets(brackets.inflatedBy(factor))

  def startOfNonZeroQualifiedRateBracket: IncomeThreshold = bracketsAscending(1)._1

end QualifiedBrackets

object QualifiedBrackets:

  def of(pairs: Iterable[(Int, Int)]): QualifiedBrackets =
    apply(
      Brackets.of(
        pairs.map { (bracketStart, ratePercentage) =>
          require(ratePercentage < 100, SourceLoc())
          IncomeThreshold(bracketStart) ->
            FederalTaxRate.unsafeFrom(ratePercentage.toDouble / 100.0d)
        }
      )
    )

  given Show[QualifiedBrackets]:
    def show(b: QualifiedBrackets): String =
      b.bracketsAscending.mkString("\n")

  given PartialOrder[QualifiedBrackets] = PartialOrder.by(_.brackets)

end QualifiedBrackets
