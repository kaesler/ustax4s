package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold

/**
  * Calculates tax on ordinary (non-investment) income.
  *
  * @param bracketStarts the tax brackets in effect
  */
final case class TaxBrackets(
  bracketStarts: Map[TMoney, TaxRate]
) {
  require(bracketStarts.contains(TMoney.zero))

  val bracketStartsAscending: Vector[(TMoney, TaxRate)] =
    bracketStarts.toVector.sortBy(_._1)

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  /**
    * @return the tax due rounded to whole dollars
    * @param m the ordinary income
    */
  def taxDueWholeDollar(m: TMoney): TMoney =
    taxDue(m).rounded

  /**
    * @return the tax due
    * @param m the ordinary income
    */
  def taxDue(m: TMoney): TMoney = {
    case class Accum(yetToBeTaxed: TMoney, taxSoFar: TMoney)
    object Accum { def initial = apply(m, TMoney.zero) }

    val accum = bracketsStartsDescending.foldLeft(Accum.initial) {

      case (Accum(yetToBeTaxed, taxSoFar), (bracketStart, bracketRate)) =>
        // Non-negative: so zero if bracket does not apply.
        val amountInThisBracket = yetToBeTaxed - bracketStart

        // Non-negative: so zero if bracket does not apply.
        val taxInThisBracket = amountInThisBracket * bracketRate
        Accum(
          yetToBeTaxed = yetToBeTaxed - amountInThisBracket,
          taxSoFar = taxSoFar + taxInThisBracket
        )
    }
    assert(accum.yetToBeTaxed.isZero)
    accum.taxSoFar
  }

  def isProgressive: Boolean = {
    val rates = bracketStartsAscending.map(_._2)
    (rates zip rates.tail)
      .forall {
        case (left, right) =>
          left < right
      }
  }
}

object TaxBrackets {

  def of(year: Year, status: FilingStatus): TaxBrackets =
    (year.getValue, status) match {
      case (2018, HeadOfHousehold) =>
        create(
          Map(
            0 -> 10,
            13600 -> 12,
            51800 -> 22,
            82500 -> 24,
            157500 -> 32,
            200000 -> 35,
            500000 -> 37
          )
        )

      case _ => ???
    }

  private def create(
    pairs: Map[Int, Int]
  ): TaxBrackets =
    TaxBrackets(
      pairs.map {
        case (bracketStart, ratePercentage) =>
          require(ratePercentage < 100)
          TMoney.u(bracketStart) ->
          TaxRate.unsafeFrom(ratePercentage.toDouble / 100.0D)
      }
    )
}
