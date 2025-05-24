package org.kae.ustax4s.gsheetfacade

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.money.Moneys.Deduction

object Conversions:
  import TypeAliases.*

  // Input argument conversions:
  given Conversion[GYear, Year] =
    (gYear: GYear) => Year.of(gYear.toInt)

  given Conversion[GFilingStatus, FilingStatus] = FilingStatus.valueOf

  given Conversion[GLocalDate, LocalDate] = (gDate: GLocalDate) =>
    LocalDate.of(
      gDate.getFullYear().toInt,
      gDate.getMonth().toInt + 1,
      gDate.getDate().toInt
    )

  // Output result conversions:
  given Conversion[Deduction, GDeduction] = _.toDouble

end Conversions
