package org.kae.ustax4s

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

object StateTaxMA extends IntMoneySyntax {

  def taxDue(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    dependents: Int
  )(
    // Excludes SocSec. So it is
    //  - earned wages
    //  - interest
    //  - dividends
    //  - capital gains
    taxableIncome: TMoney
  ): TMoney =
    TMoney.max(
      TMoney.zero,
      taxableIncome -
        totalExemptions(year, filingStatus, birthDate, dependents)
    ) * rate(year)

  private def totalExemptions(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    dependents: Int
  ): TMoney =
    personalExemption(year, filingStatus) +
      age65OrOlderExemption(year, birthDate) +
      dependentExceptions(dependents)

  // Note: Social Security is not taxed.
  private def rate(year: Year): TaxRate = {
    val r = year.getValue match {
      case 2018          => 0.051
      case 2019          => 0.0505
      case 2020          => 0.05
      case x if x > 2020 => 0.05
      case x if x < 2018 => 0.051
    }
    TaxRate.unsafeFrom(r)
  }

  private def personalExemption(
    year: Year,
    filingStatus: FilingStatus
  ): TMoney =
    (year.getValue, filingStatus) match {

      // Note: for now assume same for future years as 2020.
      case (year, fs) if year > 2020 =>
        personalExemption(Year.of(2020), fs)

      case (2020, HeadOfHousehold) => TMoney.u(6800)
      case (2020, Single)          => TMoney.u(4400)

      case (2019, HeadOfHousehold) => TMoney.u(6800)
      case (2019, Single)          => TMoney.u(4400)

      case (2018, HeadOfHousehold) => TMoney.u(6800)
      case (2018, Single)          => TMoney.u(4400)

      case _ => ???
    }

  private def age65OrOlderExemption(year: Year, birthDate: LocalDate): TMoney =
    if (isAge65OrOlder(birthDate, year))
      700.tm
    else
      TMoney.zero

  private def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    taxYear.getValue - birthDate.getYear >= 65

  private def dependentExceptions(dependents: Int): TMoney = TMoney.u(1000) mul dependents
}
