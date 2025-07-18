package org.kae.ustax4s.gsheetfacade

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.{FederalTaxRate, Regime}
import org.kae.ustax4s.money.*

object Conversions:
  import TypeAliases.*

  // Input argument conversions:
  given Conversion[GFederalTaxRate, FederalTaxRate] = FederalTaxRate.unsafeFrom

  given Conversion[GDeduction, Deduction] = Deduction.apply

  given Conversion[GFilingStatus, FilingStatus] = FilingStatus.valueOf

  given Conversion[GIncome, Income] = Income.apply

  given Conversion[GLocalDate, LocalDate] = (gDate: GLocalDate) =>
    LocalDate.of(
      gDate.getFullYear().toInt,
      gDate.getMonth().toInt + 1,
      gDate.getDate().toInt
    )

  given Conversion[GRegime, Regime] = Regime.valueOf

  given Conversion[GTaxableIncome, TaxableIncome] = TaxableIncome.apply

  given Conversion[GYear, Year] =
    (gYear: GYear) => Year.of(gYear.toInt)

  // Output result conversions:
  given Conversion[Deduction, GDeduction]             = _.asDouble
  given Conversion[IncomeThreshold, GIncomeThreshold] = _.asDouble
  given Conversion[TaxableIncome, GTaxableIncome]     = _.asDouble
  given Conversion[Income, GIncome]                   = _.asDouble
  given Conversion[TaxPayable, GTaxPayable]           = _.asDouble

end Conversions
