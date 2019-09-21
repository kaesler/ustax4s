package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold

case class TaxBrackets(bracketStarts: Map[TMoney, TaxRate]) {
  require(bracketStarts.contains(TMoney.zero))

  val bracketStartsAscending: Vector[(TMoney, TaxRate)] =
    bracketStarts.toVector.sortBy(_._1)

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  def taxDueWholeDollar(m: TMoney): TMoney =
    taxDue(m).rounded

  def taxDue(m: TMoney): TMoney = {
    val pair = bracketsStartsDescending.foldLeft(m, TMoney.zero) {

      case ((yetToBeTaxed, taxSoFar), (bracketStart, bracketRate)) =>
        // Note: because subtract does not give a a value below zero
        // both the following values will be zero if the bracket does not
        // apply.
        val amountInThisBracket = yetToBeTaxed - bracketStart
        val taxInThisBracket = amountInThisBracket * bracketRate
        (yetToBeTaxed - amountInThisBracket, taxSoFar + taxInThisBracket)
    }
    assert(pair._1.isZero)
    pair._2
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

  /**
    * For ordinary income
    * @param year
    * @param status
    * @return
    */
  def of(year: Year, status: FilingStatus): TaxBrackets =
    (year.getValue, status) match {
      case (2018, HeadOfHousehold) =>
        TaxBrackets.create(
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

  def ofPreferentialGains(year: Year, status: FilingStatus): TaxBrackets =
      (year.getValue, status) match {
      case (2018, HeadOfHousehold) =>
        TaxBrackets.create(
          Map(
            0 -> 0,
            51700 -> 15,
            452_400 -> 20
          )
        )

      case _ => ???
    }

  private def create(pairs: Map[Int, Int]): TaxBrackets =
    new TaxBrackets(
      pairs.map {
        case (bracketStart, ratePercentage) =>
          require(ratePercentage < 100)
          TMoney.u(bracketStart) ->
          TaxRate.unsafeFrom(ratePercentage.toDouble / 100.0D)
      }
    )
}
