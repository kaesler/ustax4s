package org.kae.ustax4s.federal

import cats.PartialOrder
import cats.Show
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

  @tailrec def of(year: Year, status: FilingStatus): QualifiedBrackets =
    (year.getValue, status) match

      // Note: for now assume 2022 rates into the future.
      case (year, fs) if year > 2022 => of(Year.of(2022), fs)

      case (2022, HeadOfHousehold) =>
        create(
          Map(
            0      -> 0,
            55800  -> 15,
            488500 -> 20
          )
        )

      case (2022, Single) =>
        create(
          Map(
            0      -> 0,
            41675  -> 15,
            459750 -> 20
          )
        )

      case (2021, HeadOfHousehold) =>
        create(
          Map(
            0      -> 0,
            54100  -> 15,
            473750 -> 20
          )
        )

      case (2021, Single) =>
        create(
          Map(
            0      -> 0,
            40400  -> 15,
            445850 -> 20
          )
        )

      case (2020, HeadOfHousehold) =>
        create(
          Map(
            0      -> 0,
            53600  -> 15,
            469050 -> 20
          )
        )

      case (2020, Single) =>
        create(
          Map(
            0      -> 0,
            40000  -> 15,
            442450 -> 20
          )
        )

      case (2019, HeadOfHousehold) =>
        create(
          Map(
            0      -> 0,
            52750  -> 15,
            461700 -> 20
          )
        )

      case (2019, Single) =>
        create(
          Map(
            0      -> 0,
            39375  -> 15,
            434550 -> 20
          )
        )

      case (2018, HeadOfHousehold) =>
        create(
          Map(
            0      -> 0,
            51700  -> 15,
            452400 -> 20
          )
        )

      case (2018, Single) =>
        create(
          Map(
            0      -> 0,
            38600  -> 15,
            425800 -> 20
          )
        )

      case (2017, HeadOfHousehold) =>
        create(
          Map(
            0      -> 0,
            50800  -> 15,
            444550 -> 20
          )
        )

      case (2017, Single) =>
        create(
          Map(
            0      -> 0,
            37950  -> 15,
            418400 -> 20
          )
        )

      case (2016, HeadOfHousehold) =>
        create(
          Map(
            0      -> 0,
            50400  -> 15,
            441000 -> 20
          )
        )

      case (2016, Single) =>
        create(
          Map(
            0      -> 0,
            37650  -> 15,
            415050 -> 20
          )
        )

      case _ => throw NotYetImplemented(year)

    end match