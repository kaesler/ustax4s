package org.kae.ustax4s

import java.time.{LocalDate, Year}

case class TaxRates(
  standardDeduction: TMoney,
  brackets: TaxBrackets,
  cgBrackets: CGTaxBrackets,
  filingStatus: FilingStatus
)

object TaxRates {
  def of (year: Year, filingStatus: FilingStatus, birthDate: LocalDate): TaxRates =
    TaxRates(
      StandardDeduction.of(year, filingStatus, birthDate),
      TaxBrackets.of(year, filingStatus),
      CGTaxBrackets.of(year, filingStatus),
      filingStatus
    )
}
