package org.kae.ustax4s
package federal

import cats.Show
import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.{Income, IncomeThreshold, TaxPayable}
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}
import scala.annotation.tailrec

/** Calculates tax on qualified investment income,
  * i.e. long-term capital gains and qualified dividends.
  *
  * @param thresholds
  *   the tax brackets in effect
  */
final case class QualifiedIncomeBrackets(
  thresholds: Map[IncomeThreshold, FederalTaxRate]
):
  require(isProgressive)
  require(thresholds.contains(IncomeThreshold.zero))
  require(thresholds.size >= 2)

  private def isProgressive: Boolean =
    val ratesAscending = thresholds.toList.sorted.map(_._2)
    ratesAscending.zip(ratesAscending.tail).forall { (left, right) =>
      left < right
    }

  // Adjust the thresholds for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): QualifiedIncomeBrackets =
    require(factor >= 1.0)
    QualifiedIncomeBrackets(
      thresholds.map { pair =>
        val (threshold, rate) = pair
        (threshold.increaseBy(factor).rounded, rate)
      }
    )

  val thresholdsAscending: Vector[(IncomeThreshold, FederalTaxRate)] =
    thresholds.toVector.sortBy(_._1)

  require(thresholdsAscending(0) == (IncomeThreshold.zero, FederalTaxRate.unsafeFrom(0.0)))

  private val thresholdsDescending = thresholdsAscending.reverse

  def startOfNonZeroQualifiedRateBracket: IncomeThreshold = thresholdsAscending(1)._1

  def taxDueWholeDollar(
    taxableOrdinaryIncome: Income,
    qualifiedIncome: Income
  ): TaxPayable =
    taxDue(taxableOrdinaryIncome, qualifiedIncome).rounded

  def taxDue(
    taxableOrdinaryIncome: Income,
    qualifiedIncome: Income
  ): TaxPayable =
    case class Accum(
      totalIncomeInHigherBrackets: Income,
      gainsYetToBeTaxed: Income,
      gainsTaxSoFar: TaxPayable
    )
    object Accum:
      def initial: Accum =
        apply(Income.zero, qualifiedIncome, TaxPayable.zero)

    val totalTaxableIncome = taxableOrdinaryIncome + qualifiedIncome
    val accum =
      thresholdsDescending.foldLeft(Accum.initial) {
        case (
              Accum(
                totalIncomeInHigherBrackets,
                gainsYetToBeTaxed,
                gainsTaxSoFar
              ),
              (bracketThreshold, bracketRate)
            ) =>
          val totalIncomeYetToBeTaxed =
            totalTaxableIncome reduceBy totalIncomeInHigherBrackets
          val ordinaryIncomeYetToBeTaxed =
            totalIncomeYetToBeTaxed reduceBy gainsYetToBeTaxed

          // Non-negative: so zero if bracket does not apply.
          val totalIncomeInThisBracket = totalIncomeYetToBeTaxed amountAbove bracketThreshold

          // Non-negative: so zero if bracket does not apply.
          val ordinaryIncomeInThisBracket =
            ordinaryIncomeYetToBeTaxed amountAbove bracketThreshold

          val gainsInThisBracket: Income =
            totalIncomeInThisBracket reduceBy ordinaryIncomeInThisBracket
          val taxInThisBracket = gainsInThisBracket taxAt bracketRate
          Accum(
            totalIncomeInHigherBrackets = totalIncomeInHigherBrackets + totalIncomeInThisBracket,
            gainsYetToBeTaxed = gainsYetToBeTaxed reduceBy gainsInThisBracket,
            gainsTaxSoFar = gainsTaxSoFar + taxInThisBracket
          )
      }
    assert(accum.totalIncomeInHigherBrackets == totalTaxableIncome)
    assert(accum.gainsYetToBeTaxed.isZero)
    accum.gainsTaxSoFar

  def bracketExists(bracketRate: FederalTaxRate): Boolean =
    thresholdsAscending.exists { (_, rate) => rate == bracketRate }

object QualifiedIncomeBrackets:

  given Show[QualifiedIncomeBrackets] with
    def show(b: QualifiedIncomeBrackets): String =
      b.thresholdsAscending.mkString("\n")

  @tailrec def of(year: Year, status: FilingStatus): QualifiedIncomeBrackets =
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

  private def create(pairs: Map[Int, Int]): QualifiedIncomeBrackets =
    QualifiedIncomeBrackets(
      pairs.map { (bracketStart, ratePercentage) =>
        require(ratePercentage < 100)
        IncomeThreshold(bracketStart) ->
          FederalTaxRate.unsafeFrom(ratePercentage.toDouble / 100.0d)
      }
    )
