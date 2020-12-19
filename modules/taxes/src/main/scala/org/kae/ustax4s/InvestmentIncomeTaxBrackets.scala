package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

/**
  * Calculates tax on qualified investment income,
  * i.e. long-term capital gains and qualified dividends.
  *
  * @param bracketStarts the tax brackets in effect
  */
final case class InvestmentIncomeTaxBrackets(
  bracketStarts: Map[TMoney, TaxRate]
) {
  require(bracketStarts.contains(TMoney.zero))

  val bracketStartsAscending: Vector[(TMoney, TaxRate)] =
    bracketStarts.toVector.sortBy(_._1)

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  def show: String = {
    bracketStartsAscending.mkString("\n")
  }

  /**
    * @return the tax due rounded to whole dollars
    * @param qualifiedInvestmentIncome the investment income
    */
  def taxDueWholeDollar(
    taxableOrdinaryIncome: TMoney,
    qualifiedInvestmentIncome: TMoney
  ): TMoney =
    taxDue(taxableOrdinaryIncome, qualifiedInvestmentIncome).rounded

  def taxDue(
    taxableOrdinaryIncome: TMoney,
    qualifiedInvestmentIncome: TMoney
  ): TMoney = {

    case class Accum(
      totalIncomeInHigherBrackets: TMoney,
      gainsYetToBeTaxed: TMoney,
      gainsTaxSoFar: TMoney
    )
    object Accum {
      def initial: Accum = apply(TMoney.zero, qualifiedInvestmentIncome, TMoney.zero)
    }

    val totalIncome = taxableOrdinaryIncome + qualifiedInvestmentIncome
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
          val totalIncomeYetToBeTaxed = totalIncome - totalIncomeInHigherBrackets
          val ordinaryIncomeYetToBeTaxed = totalIncomeYetToBeTaxed - gainsYetToBeTaxed

          // Non-negative: so zero if bracket does not apply.
          val totalIncomeInThisBracket = totalIncomeYetToBeTaxed - bracketStart

          // Non-negative: so zero if bracket does not apply.
          val ordinaryIncomeInThisBracket = ordinaryIncomeYetToBeTaxed - bracketStart

          val gainsInThisBracket: TMoney = totalIncomeInThisBracket - ordinaryIncomeInThisBracket
          val taxInThisBracket = gainsInThisBracket * bracketRate
          Accum(
            totalIncomeInHigherBrackets =
              totalIncomeInHigherBrackets + totalIncomeInThisBracket,
            gainsYetToBeTaxed = gainsYetToBeTaxed - gainsInThisBracket,
            gainsTaxSoFar = gainsTaxSoFar + taxInThisBracket
          )
      }
    assert(accum.totalIncomeInHigherBrackets == totalIncome)
    assert(accum.gainsYetToBeTaxed.isZero)
    accum.gainsTaxSoFar
  }
}

object InvestmentIncomeTaxBrackets {

  def of(year: Year, status: FilingStatus): InvestmentIncomeTaxBrackets =
    (year.getValue, status) match {
      case (2021, HeadOfHousehold) =>
        create(
          Map(
            0 -> 0,
            54100 -> 15,
            473750 -> 20
          )
        )
      case (2021, Single) =>
        create(
          Map(
            0 -> 0,
            40400 -> 15,
            445850 -> 20
          )
        )
      case (2020, HeadOfHousehold) =>
        create(
          Map(
            0 -> 0,
            53600 -> 15,
            469050 -> 20
          )
        )
      case (2019, HeadOfHousehold) =>
        create(
          Map(
            0 -> 0,
            52750 -> 15,
            461700 -> 20
          )
        )
      case (2018, HeadOfHousehold) =>
        create(
          Map(
            0 -> 0,
            51700 -> 15,
            452400 -> 20
          )
        )

      // TODO: for now assume 2021 rates into the future.
      case (year, fs) if year > 2021 => of(Year.of(2021), fs)

      // Fail for non-applicable years.
      case (year, _) if year < 2018 => ???
    }

  private def create(
    pairs: Map[Int, Int]
  ): InvestmentIncomeTaxBrackets =
    InvestmentIncomeTaxBrackets(
      pairs.map {
        case (bracketStart, ratePercentage) =>
          require(ratePercentage < 100)
          TMoney.u(bracketStart) ->
          TaxRate.unsafeFrom(ratePercentage.toDouble / 100.0D)
      }
    )
}
