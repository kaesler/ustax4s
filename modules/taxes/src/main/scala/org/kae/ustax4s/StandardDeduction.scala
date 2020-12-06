package org.kae.ustax4s

import java.time.{LocalDate, Month, Year}
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

object StandardDeduction {

  def of(year: Year, filingStatus: FilingStatus, birthDate: LocalDate): TMoney =
    unadjustedForAge(year, filingStatus) +
      (if (isAge65OrOlder(birthDate, year)) TMoney.u(1350) else TMoney.zero)

  def unadjustedForAge(year: Year, filingStatus: FilingStatus): TMoney =
    (year.getValue, filingStatus) match {
      case (2021, HeadOfHousehold) => TMoney.u(18800)
      case (2020, HeadOfHousehold) => TMoney.u(18650)
      case (2019, HeadOfHousehold) => TMoney.u(18350)
      case (2018, HeadOfHousehold) => TMoney.u(18000)

      case (2021, Single) => TMoney.u(12550)
      case (2020, Single) => TMoney.u(12400)
      case (2019, Single) => TMoney.u(12200)
      case (2018, Single) => TMoney.u(12000)
      case _ => ???
    }

  private def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    birthDate.isBefore(
     LocalDate.of(taxYear.getValue, Month.JANUARY.getValue, 2)
       .minusYears(65)
    )
}