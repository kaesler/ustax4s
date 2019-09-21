package org.kae.ustax4s

import java.time.Year

case class TaxRates(
  standardDeduction: TMoney,
  brackets: TaxBrackets
)

object TaxRates {
  def of (year: Year, filingStatus: FilingStatus): TaxRates =
    TaxRates(
      StandardDeduction.of(year, filingStatus),
      TaxBrackets.of(year, filingStatus)
    )
}
