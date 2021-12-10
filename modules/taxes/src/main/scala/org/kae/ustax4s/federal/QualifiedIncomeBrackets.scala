package org.kae.ustax4s
package federal

import cats.Show
import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.{FilingStatus, NotYetImplemented}
import scala.annotation.tailrec

/** Calculates tax on qualified investment income,
  * i.e. long-term capital gains and qualified dividends.
  *
  * @param bracketStarts
  *   the tax brackets in effect
  */
final case class QualifiedIncomeBrackets(
  bracketStarts: Map[Money, FederalTaxRate]
):
  // Note: We capture "tax free LTCGs with suitably low income" by having a
  // zero-rate lowest bracket.
  require(bracketStarts.contains(Money(0)))
  require(bracketStarts.size >= 2)

  // Adjust the bracket starts for inflation.
  // E.g. for 2% inflation: inflated(1.02)
  def inflatedBy(factor: Double): QualifiedIncomeBrackets =
    require(factor >= 1.0)
    QualifiedIncomeBrackets(
      bracketStarts.map { (start, rate) =>
        ((start mul factor).rounded, rate)
      }
    )

  val bracketStartsAscending: Vector[(Money, FederalTaxRate)] =
    bracketStarts.toVector.sortBy(_._1)

  require(bracketStartsAscending(0) == (Money(0), FederalTaxRate.unsafeFrom(0.0)))

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  def startOfNonZeroQualifiedRateBracket: Money = bracketStartsAscending(1)._1

  def taxDueWholeDollar(
    taxableOrdinaryIncome: Money,
    qualifiedIncome: Money
  ): Money =
    taxDue(taxableOrdinaryIncome, qualifiedIncome).rounded

  def taxDue(
    taxableOrdinaryIncome: Money,
    qualifiedIncome: Money
  ): Money = taxDueFunctionally(taxableOrdinaryIncome, qualifiedIncome)

  def taxDueFunctionally(
    taxableOrdinaryIncome: Money,
    qualifiedIncome: Money
  ): Money =
    case class Accum(
      totalIncomeInHigherBrackets: Money,
      gainsYetToBeTaxed: Money,
      gainsTaxSoFar: Money
    )
    object Accum:
      def initial: Accum =
        apply(Money.zero, qualifiedIncome, Money.zero)

    val totalTaxableIncome = taxableOrdinaryIncome + qualifiedIncome
    val accum =
      bracketsStartsDescending.foldLeft(Accum.initial) {
        case (
              Accum(
                totalIncomeInHigherBrackets,
                gainsYetToBeTaxed,
                gainsTaxSoFar
              ),
              (bracketStart, bracketRate)
            ) =>
          val totalIncomeYetToBeTaxed =
            totalTaxableIncome subp totalIncomeInHigherBrackets
          val ordinaryIncomeYetToBeTaxed =
            totalIncomeYetToBeTaxed subp gainsYetToBeTaxed

          // Non-negative: so zero if bracket does not apply.
          val totalIncomeInThisBracket = totalIncomeYetToBeTaxed subp bracketStart

          // Non-negative: so zero if bracket does not apply.
          val ordinaryIncomeInThisBracket =
            ordinaryIncomeYetToBeTaxed subp bracketStart

          val gainsInThisBracket: Money =
            totalIncomeInThisBracket subp ordinaryIncomeInThisBracket
          val taxInThisBracket = gainsInThisBracket taxAt bracketRate
          Accum(
            totalIncomeInHigherBrackets = totalIncomeInHigherBrackets + totalIncomeInThisBracket,
            gainsYetToBeTaxed = gainsYetToBeTaxed subp gainsInThisBracket,
            gainsTaxSoFar = gainsTaxSoFar + taxInThisBracket
          )
      }
    assert(accum.totalIncomeInHigherBrackets == totalTaxableIncome)
    assert(accum.gainsYetToBeTaxed.isZero)
    accum.gainsTaxSoFar

  // Note: kept here for translation to TypeScript.
  def taxDueImperatively(
    taxableOrdinaryIncome: Money,
    qualifiedIncome: Money
  ): Money =
    var totalIncomeInHigherBrackets = Money(0)
    var gainsYetToBeTaxed           = qualifiedIncome
    var gainsTaxSoFar               = Money(0)

    val totalIncome = taxableOrdinaryIncome + qualifiedIncome
    bracketsStartsDescending.foreach { (bracketStart, bracketRate) =>
      val totalIncomeYetToBeTaxed = totalIncome subp totalIncomeInHigherBrackets
      val ordinaryIncomeYetToBeTaxed =
        totalIncomeYetToBeTaxed subp gainsYetToBeTaxed

      // Non-negative: so zero if bracket does not apply.
      val totalIncomeInThisBracket = totalIncomeYetToBeTaxed subp bracketStart

      // Non-negative: so zero if bracket does not apply.
      val ordinaryIncomeInThisBracket =
        ordinaryIncomeYetToBeTaxed subp bracketStart

      val gainsInThisBracket: Money =
        totalIncomeInThisBracket subp ordinaryIncomeInThisBracket
      val taxInThisBracket = gainsInThisBracket taxAt bracketRate
      totalIncomeInHigherBrackets = totalIncomeInHigherBrackets + totalIncomeInThisBracket
      gainsYetToBeTaxed = gainsYetToBeTaxed subp gainsInThisBracket
      gainsTaxSoFar = gainsTaxSoFar + taxInThisBracket

    }
    assert(totalIncomeInHigherBrackets == totalIncome)
    assert(gainsYetToBeTaxed.isZero)
    val res = gainsTaxSoFar

//    println(
//      s"taxDueOnInvestments(${taxableOrdinaryIncome}, $qualifiedInvestmentIncome): $res"
//    )
    res

  def bracketExists(bracketRate: FederalTaxRate): Boolean =
    bracketStartsAscending.exists { (_, rate) => rate == bracketRate }

object QualifiedIncomeBrackets:

  given Show[QualifiedIncomeBrackets] with
    def show(b: QualifiedIncomeBrackets): String =
      b.bracketStartsAscending.mkString("\n")

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
        Money(bracketStart) ->
          FederalTaxRate.unsafeFrom(ratePercentage.toDouble / 100.0d)
      }
    )
