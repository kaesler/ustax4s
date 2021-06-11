package org.kae.ustax4s
package federal

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.{FilingStatus, TMoney}
import scala.annotation.tailrec

/** Calculates tax on qualified investment income,
  * i.e. long-term capital gains and qualified dividends.
  *
  * @param bracketStarts
  *   the tax brackets in effect
  */
final case class QualifiedIncomeBrackets(
  bracketStarts: Map[TMoney, FederalTaxRate]
):
  // Note: We capture "tax free LTCGs with suitably low income" by having a
  // zero-rate lowest bracket.
  require(bracketStarts.contains(TMoney.zero))
  require(bracketStarts.size >= 2)

  val bracketStartsAscending: Vector[(TMoney, FederalTaxRate)] =
    bracketStarts.toVector.sortBy(_._1)

  require(bracketStartsAscending(0) == (TMoney.zero, FederalTaxRate.unsafeFrom(0.0)))

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  def show: String = {
    bracketStartsAscending.mkString("\n")
  }

  def startOfNonZeroQualifiedRateBracket: TMoney = bracketStartsAscending(1)._1

  /** @return
    *   the tax due rounded to whole dollars
    * @param qualifiedIncome
    *   the qualified income
    */
  def taxDueWholeDollar(
    taxableOrdinaryIncome: TMoney,
    qualifiedIncome: TMoney
  ): TMoney =
    taxDueFunctionally(taxableOrdinaryIncome, qualifiedIncome).rounded

  def taxDueFunctionally(
    taxableOrdinaryIncome: TMoney,
    qualifiedIncome: TMoney
  ): TMoney =
    case class Accum(
      totalIncomeInHigherBrackets: TMoney,
      gainsYetToBeTaxed: TMoney,
      gainsTaxSoFar: TMoney
    )
    object Accum:
      def initial: Accum =
        apply(TMoney.zero, qualifiedIncome, TMoney.zero)

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
            totalTaxableIncome - totalIncomeInHigherBrackets
          val ordinaryIncomeYetToBeTaxed =
            totalIncomeYetToBeTaxed - gainsYetToBeTaxed

          // Non-negative: so zero if bracket does not apply.
          val totalIncomeInThisBracket = totalIncomeYetToBeTaxed - bracketStart

          // Non-negative: so zero if bracket does not apply.
          val ordinaryIncomeInThisBracket =
            ordinaryIncomeYetToBeTaxed - bracketStart

          val gainsInThisBracket: TMoney =
            totalIncomeInThisBracket - ordinaryIncomeInThisBracket
          val taxInThisBracket = gainsInThisBracket taxAt bracketRate
          Accum(
            totalIncomeInHigherBrackets = totalIncomeInHigherBrackets + totalIncomeInThisBracket,
            gainsYetToBeTaxed = gainsYetToBeTaxed - gainsInThisBracket,
            gainsTaxSoFar = gainsTaxSoFar + taxInThisBracket
          )
      }
    assert(accum.totalIncomeInHigherBrackets == totalTaxableIncome)
    assert(accum.gainsYetToBeTaxed.isZero)
    val res = accum.gainsTaxSoFar

//    println(
//      s"taxDueOnInvestments(${taxableOrdinaryIncome}, $qualifiedInvestmentIncome): $res"
//    )
    res

  def taxDueImperatively(
    taxableOrdinaryIncome: TMoney,
    qualifiedIncome: TMoney
  ): TMoney =
    var totalIncomeInHigherBrackets = TMoney.zero
    var gainsYetToBeTaxed           = qualifiedIncome
    var gainsTaxSoFar               = TMoney.zero

    val totalIncome = taxableOrdinaryIncome + qualifiedIncome
    bracketsStartsDescending.foreach { (bracketStart, bracketRate) =>
      val totalIncomeYetToBeTaxed = totalIncome - totalIncomeInHigherBrackets
      val ordinaryIncomeYetToBeTaxed =
        totalIncomeYetToBeTaxed - gainsYetToBeTaxed

      // Non-negative: so zero if bracket does not apply.
      val totalIncomeInThisBracket = totalIncomeYetToBeTaxed - bracketStart

      // Non-negative: so zero if bracket does not apply.
      val ordinaryIncomeInThisBracket =
        ordinaryIncomeYetToBeTaxed - bracketStart

      val gainsInThisBracket: TMoney =
        totalIncomeInThisBracket - ordinaryIncomeInThisBracket
      val taxInThisBracket = gainsInThisBracket taxAt bracketRate
      totalIncomeInHigherBrackets = totalIncomeInHigherBrackets + totalIncomeInThisBracket
      gainsYetToBeTaxed = gainsYetToBeTaxed - gainsInThisBracket
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

  @tailrec def of(year: Year, status: FilingStatus): QualifiedIncomeBrackets =
    (year.getValue, status) match

      // Note: for now assume 2021 rates into the future.
      case (year, fs) if year > 2021 => of(Year.of(2021), fs)

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
      case (2019, HeadOfHousehold) =>
        create(
          Map(
            0      -> 0,
            52750  -> 15,
            461700 -> 20
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

      case _ => ???
    end match

  private def create(
    pairs: Map[Int, Int]
  ): QualifiedIncomeBrackets =
    QualifiedIncomeBrackets(
      pairs.map { (bracketStart, ratePercentage) =>
        require(ratePercentage < 100)
        TMoney(bracketStart) ->
          FederalTaxRate.unsafeFrom(ratePercentage.toDouble / 100.0d)
      }
    )