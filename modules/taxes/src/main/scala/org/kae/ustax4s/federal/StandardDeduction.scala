package org.kae.ustax4s.federal

import java.time.{LocalDate, Month, Year}
import org.kae.ustax4s.money.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*

object StandardDeduction:

  def of(year: Year, filingStatus: FilingStatus, birthDate: LocalDate): Money =
    unadjustedForAge(year, filingStatus) +
      (if isAge65OrOlder(birthDate, year) then Money(1350) else Money.zero)

  private def unadjustedForAge(year: Year, filingStatus: FilingStatus): Money =
    (year.getValue, filingStatus) match

      // Note: for now assume 2021 rates into the future.
      case (year, fs) if year > 2021 => unadjustedForAge(Year.of(2021), fs)

      case (2021, HeadOfHousehold) => Money(18800)
      case (2020, HeadOfHousehold) => Money(18650)
      case (2019, HeadOfHousehold) => Money(18350)
      case (2018, HeadOfHousehold) => Money(18000)

      case (2021, Single) => Money(12550)
      case (2020, Single) => Money(12400)
      case (2019, Single) => Money(12200)
      case (2018, Single) => Money(12000)

      case _ => ???

  private def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    birthDate.isBefore(
      LocalDate
        .of(taxYear.getValue, Month.JANUARY.getValue, 2)
        .minusYears(65)
    )
