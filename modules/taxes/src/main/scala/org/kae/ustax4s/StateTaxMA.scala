package org.kae.ustax4s

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

object StateTaxMA extends IntMoneySyntax {

  // TODO tests for this.

  // Note: Social Security is not taxed.
  private val rate = TaxRate.unsafeFrom(0.051)

  private def personalExemption(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate
  ): TMoney = {
    val unadjustedForAge = (year.getValue, filingStatus) match {
      case (2020, HeadOfHousehold) => TMoney.u(6800)
      case (2020, Single)          => TMoney.u(4400)

      case (2019, HeadOfHousehold) => TMoney.u(6800)
      case (2019, Single)          => TMoney.u(4400)

      case (2018, HeadOfHousehold) => TMoney.u(6800)
      case (2018, Single)          => TMoney.u(4400)

      // TODO: for now assume same for future years as 2020.
      case (year, fs) if year > 2020 =>
        personalExemption(Year.of(2020), fs, birthDate)
      // Fail for inapplicable past years.
      case (year, _) if year < 2018 => ???
    }

    unadjustedForAge + (if (isAge65OrOlder(birthDate, year)) 700.tm
                        else TMoney.zero)
  }

  private def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    taxYear.getValue - birthDate.getYear >= 65

  def taxDue(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    dependents: Int
  )(
    // Excludes SocSec
    taxableIncome: TMoney
  ): TMoney = {
    TMoney.max(
      TMoney.zero,
      taxableIncome -
        personalExemption(year, filingStatus, birthDate) -
        (TMoney.u(1000) mul dependents)
    ) * rate
  }
}
