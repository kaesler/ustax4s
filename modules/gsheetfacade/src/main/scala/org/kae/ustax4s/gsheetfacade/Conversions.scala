package org.kae.ustax4s.gsheetfacade

import gsheets.cells.{GSheetCellValue, GSheetGrid, ScalaCellGrid}
import gsheets.customfunctions.Input
import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.FedTaxRate
import org.kae.ustax4s.money.NonNegMoneys.*
import scala.scalajs.js

object Conversions:

  type Output = GSheetCellValue | GSheetGrid

  // Conversions from Input.
  given Conversion[Input, FedTaxRate] =
    doubleInputConversion[FedTaxRate](
      "FederalTaxRate",
      FedTaxRate.unsafeFrom
    )

  given Conversion[Input, Int]          = doubleInputConversion("Int", _.toInt)
  given Conversion[Input, Double]       = doubleInputConversion("Double", identity)
  given Conversion[Input, Deduction]    = doubleInputConversion("Deduction", Deduction.apply)
  given Conversion[Input, FilingStatus] =
    stringInputConversion("FilingStatus", FilingStatus.valueOf)
  given Conversion[Input, Income] =
    doubleInputConversion("Income", Income.apply)

  given Conversion[Input, LocalDate] =
    dateInputConversion(
      "LocalDate",
      jsd =>
        LocalDate.of(
          jsd.getFullYear().toInt,
          jsd.getMonth().toInt + 1,
          jsd.getDate().toInt
        )
    )
  end given

  given Conversion[Input, TaxableIncome] =
    doubleInputConversion("TaxableIncome", TaxableIncome.apply)

  given Conversion[Input, Year] =
    doubleInputConversion(
      "Year",
      (d: Double) => Year.of(d.toInt)
    )
  end given

  given Conversion[Input, ScalaCellGrid] =
    case g: GSheetGrid => ScalaCellGrid(g)
    case badCellValue  =>
      throw new IllegalArgumentException(
        s"argument must be a range of cells: $badCellValue"
      )
  end given

  // Conversions to Output.
  given Conversion[Deduction, Output]       = _.asDouble
  given Conversion[IncomeThreshold, Output] = _.asDouble
  given Conversion[TaxableIncome, Output]   = _.asDouble
  given Conversion[Income, Output]          = _.asDouble
  given Conversion[TaxPayable, Output]      = _.asDouble
  given Conversion[ScalaCellGrid, Output]   = GSheetGrid.apply

  private def dateInputConversion[T](
    typeName: String,
    constructor: js.Date => T
  ): Conversion[Input, T] =
    case date: js.Date => constructor(date)
    case badCellValue  =>
      throw new IllegalArgumentException(
        s"$typeName argument must be a js.Date: $badCellValue"
      )
  end dateInputConversion

  private def doubleInputConversion[T](
    typeName: String,
    constructor: Double => T
  ): Conversion[Input, T] =
    case d: Double    => constructor(d)
    case badCellValue =>
      throw new IllegalArgumentException(
        s"$typeName argument must be a number: $badCellValue"
      )
  end doubleInputConversion

  private def stringInputConversion[T](
    typeName: String,
    constructor: String => T
  ): Conversion[Input, T] =
    case s: String    => constructor(s)
    case badCellValue =>
      throw new IllegalArgumentException(
        s"$typeName argument must be a String: $badCellValue"
      )
  end stringInputConversion

end Conversions
