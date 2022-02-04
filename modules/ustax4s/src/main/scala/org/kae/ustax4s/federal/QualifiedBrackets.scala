package org.kae.ustax4s.federal

import cats.{PartialOrder, Show}
import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.{Income, IncomeThreshold, TaxPayable}
import org.kae.ustax4s.taxfunction.TaxFunction
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}
import scala.annotation.tailrec
import scala.math.Ordering.Implicits.infixOrderingOps

// Note: contain a Brackets[FederalTaxRate] rather than opaque type
// so we can precompute and store val properties.
final case class QualifiedBrackets(
  brackets: Brackets[FederalTaxRate]
):
  require(brackets.isProgressive, brackets.toString)
  require(brackets.contains(IncomeThreshold.zero))
  require(brackets.size >= 2)

  val bracketsAscending: Vector[(IncomeThreshold, FederalTaxRate)] =
    brackets.bracketsAscending
  require(bracketsAscending(0) == (IncomeThreshold.zero, FederalTaxRate.unsafeFrom(0.0)))

  // Adjust the thresholds for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): QualifiedBrackets =
    QualifiedBrackets(brackets.inflatedBy(factor))

  def startOfNonZeroQualifiedRateBracket: IncomeThreshold = bracketsAscending(1)._1

end QualifiedBrackets

object QualifiedBrackets:

  def create(pairs: Iterable[(Int, Int)]): QualifiedBrackets =
    apply(
      Brackets.create(
        pairs.map { (bracketStart, ratePercentage) =>
          require(ratePercentage < 100)
          IncomeThreshold(bracketStart) ->
            FederalTaxRate.unsafeFrom(ratePercentage.toDouble / 100.0d)
        }
      )
    )

  given Show[QualifiedBrackets] with
    def show(b: QualifiedBrackets): String =
      b.bracketsAscending.mkString("\n")

  given [R]: PartialOrder[QualifiedBrackets] = PartialOrder.by(_.brackets)

end QualifiedBrackets
