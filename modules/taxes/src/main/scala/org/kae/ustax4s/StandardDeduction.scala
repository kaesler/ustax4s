package org.kae.ustax4s

import java.time.{LocalDate, Month, Year}
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

object StandardDeduction:

  def of(year: Year, filingStatus: FilingStatus, birthDate: LocalDate): TMoney =
    unadjustedForAge(year, filingStatus) +
      (if isAge65OrOlder(birthDate, year) then TMoney(1350) else TMoney.zero)

  private def unadjustedForAge(year: Year, filingStatus: FilingStatus): TMoney =
    (year.getValue, filingStatus) match

      // Note: for now assume 2021 rates into the future.
      case (year, fs) if year > 2021 => unadjustedForAge(Year.of(2021), fs)

      case (2021, HeadOfHousehold) => TMoney(18800)
      case (2020, HeadOfHousehold) => TMoney(18650)
      case (2019, HeadOfHousehold) => TMoney(18350)
      case (2018, HeadOfHousehold) => TMoney(18000)

      case (2021, Single) => TMoney(12550)
      case (2020, Single) => TMoney(12400)
      case (2019, Single) => TMoney(12200)
      case (2018, Single) => TMoney(12000)

      case _ => ???

  private def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    birthDate.isBefore(
      LocalDate
        .of(taxYear.getValue, Month.JANUARY.getValue, 2)
        .minusYears(65)
    )
