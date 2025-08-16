package org.kae.ustax4s.gsheetfacade

import gsheets.cells.GSheetGrid
import gsheets.customfunctions.{Decoder, Encoder}
import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.FederalTaxRate
import org.kae.ustax4s.money.Moneys.{Deduction, Income, TaxableIncome}
import scala.scalajs.js
import scala.util.{Failure, Try}

object Codecs:

  given Encoder[FilingStatus] =
    (grid: GSheetGrid) =>
      grid(0)(0) match
        case s: String =>
          Try(FilingStatus.valueOf(s))
        case badCellValue =>
          Failure(
            new IllegalArgumentException(
              s"""FilingStatus argument must be a String: "$badCellValue""""
            )
          )
  end given

  given Encoder[Income] = numericEncoder("Income", Income.apply)
  given Encoder[Deduction] = numericEncoder("Deduction", Deduction.apply)
  given Encoder[TaxableIncome] = numericEncoder("TaxableIncome", TaxableIncome.apply)

  given Encoder[FederalTaxRate] =
    (grid: GSheetGrid) =>
      Try:
        grid(0)(0) match
          case d: Double => FederalTaxRate.unsafeFrom(d)
          case badCellValue =>
            throw new IllegalArgumentException(
              s"FederalTaxRate argument must be a number: $badCellValue"
            )
  end given

  given Encoder[LocalDate] =
    (grid: GSheetGrid) =>
      Try:
        grid(0)(0) match
          case d: js.Date =>
            LocalDate.of(
              d.getFullYear().toInt,
              d.getMonth().toInt + 1,
              d.getDate().toInt
            )
          case badCellValue =>
            throw new IllegalArgumentException(
              s"LocalDate argument must be a date: $badCellValue"
            )
  end given

  given Encoder[Year] =
    (grid: GSheetGrid) =>
      Try:
        grid(0)(0) match
          case d: Double => Year.of(d.toInt)
          case badCellValue =>
            throw new IllegalArgumentException(
              s"""Year argument must be a number: "$badCellValue""""
            )
  end given

  private def numericEncoder[T](
    typeName: String,
    constructor: Double => T
  ): Encoder[T] =
    (grid: GSheetGrid) =>
      Try:
        grid(0)(0) match
          case d: Double => constructor(d)
          case badCellValue =>
            throw new IllegalArgumentException(
              s"$typeName argument must be a number: $badCellValue"
            )
  end numericEncoder

  given Decoder[Deduction] = numericDecoder[Deduction](_.asDouble)
  given Decoder[Income] = numericDecoder[Income](_.asDouble)

  private def numericDecoder[T](
    constructor: T => Double
  ): Decoder[T] =
    (t: T) => summon[Decoder[Double]].decodeU(constructor(t))
  end numericDecoder

end Codecs


