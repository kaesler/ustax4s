package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

/**
  * Calculates tax on ordinary (non-investment) income.
  *
  * @param bracketStarts the tax brackets in effect
  */
final case class OrdinaryIncomeTaxBrackets(
  bracketStarts: Map[TMoney, TaxRate]
) {
  require(bracketStarts.contains(TMoney.zero))

  val bracketStartsAscending: Vector[(TMoney, TaxRate)] =
    bracketStarts.toVector.sortBy(_._1)

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  /**
    * @return the tax due on the ordinary income (not LTCGs...)
    * @param taxableOrdinaryIncome the ordinary income
    */
  def taxDue(
    taxableOrdinaryIncome: TMoney
  ): TMoney =
    taxDueFunctionally(taxableOrdinaryIncome)

  private def taxDueFunctionally(
    taxableOrdinaryIncome: TMoney
  ): TMoney = {

    // Note: Qualified investment income sort of occupies the top brackets above
    // ordinary income and so does not affect this.

    case class Accum(ordinaryIncomeYetToBeTaxed: TMoney, taxSoFar: TMoney)
    object Accum {
      def initial: Accum = apply(taxableOrdinaryIncome, TMoney.zero)
    }

    val accum = bracketsStartsDescending.foldLeft(Accum.initial) {

      case (
          Accum(ordinaryIncomeYetToBeTaxed, taxSoFar),
          (bracketStart, bracketRate)
          ) =>
        // Result will be non-negative: so becomes zero if bracket does not apply.
        val ordinaryIncomeInThisBracket = ordinaryIncomeYetToBeTaxed - bracketStart

        // Non-negative: so zero if bracket does not apply.
        val taxInThisBracket = ordinaryIncomeInThisBracket * bracketRate
        Accum(
          ordinaryIncomeYetToBeTaxed = ordinaryIncomeYetToBeTaxed - ordinaryIncomeInThisBracket,
          taxSoFar = taxSoFar + taxInThisBracket
        )
    }
    assert(accum.ordinaryIncomeYetToBeTaxed.isZero)
    accum.taxSoFar
  }

  def taxDueImperatively(
    taxableOrdinaryIncome: TMoney
  ): TMoney = {
    var ordinaryIncomeYetToBeTaxed = taxableOrdinaryIncome
    var taxSoFar = TMoney.zero
    bracketsStartsDescending.foreach { case (bracketStart, bracketRate) =>

      // Result will be non-negative: so becomes zero if bracket does not apply.
      val ordinaryIncomeInThisBracket = ordinaryIncomeYetToBeTaxed - bracketStart

      // Non-negative: so zero if bracket does not apply.
      val taxInThisBracket = ordinaryIncomeInThisBracket * bracketRate

      ordinaryIncomeYetToBeTaxed = ordinaryIncomeYetToBeTaxed - ordinaryIncomeInThisBracket
      taxSoFar = taxSoFar + taxInThisBracket
    }
    assert(ordinaryIncomeYetToBeTaxed.isZero)
    taxSoFar
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

object OrdinaryIncomeTaxBrackets {

  def of(year: Year, status: FilingStatus): OrdinaryIncomeTaxBrackets =
    (year.getValue, status) match {

      case (2021, HeadOfHousehold) =>
        create(
          Map(
            0 -> 10,
            14200 -> 12,
            54200 -> 22,
            86350 -> 24,
            164900 -> 32,
            209400 -> 35,
            523600 -> 37
          )
        )

      case (2021, Single) =>
        create(
          Map(
            0 -> 10,
            9950 -> 12,
            40525 -> 22,
            86375 -> 24,
            164925 -> 32,
            209425 -> 35,
            523600 -> 37
          )
        )


      case (2020, HeadOfHousehold) =>
        create(
          Map(
            0 -> 10,
            14100 -> 12,
            53700 -> 22,
            85500 -> 24,
            163300 -> 32,
            207350 -> 35,
            518400 -> 37
          )
        )

      case (2019, HeadOfHousehold) =>
        create(
          Map(
            0 -> 10,
            13850 -> 12,
            52850 -> 22,
            84200 -> 24,
            160700 -> 32,
            204100 -> 35,
            510300 -> 37
          )
        )

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
  ): OrdinaryIncomeTaxBrackets =
    OrdinaryIncomeTaxBrackets(
      pairs.map {
        case (bracketStart, ratePercentage) =>
          require(ratePercentage < 100)
          TMoney.u(bracketStart) ->
          TaxRate.unsafeFrom(ratePercentage.toDouble / 100.0D)
      }
    )
}
