package org.kae.ustax4s

import java.time.{LocalDate, Month, Year}
import org.kae.ustax4s.FilingStatus.HeadOfHousehold

object StandardDeduction {

  def of(year: Year, filingStatus: FilingStatus, birthDate: LocalDate): TMoney =
    unadjustedForAge(year, filingStatus) +
      (if (isAge65OrOlder(birthDate, year)) TMoney.u(1300) else TMoney.zero)

  def unadjustedForAge(year: Year, filingStatus: FilingStatus): TMoney =
    (year.getValue, filingStatus) match {
      case (2021, HeadOfHousehold) => TMoney.u(18800)
      case (2020, HeadOfHousehold) => TMoney.u(18650)
      case (2019, HeadOfHousehold) => TMoney.u(18350)
      case (2018, HeadOfHousehold) => TMoney.u(18000)
      case _ => ???
    }

  private def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    birthDate.isBefore(
     LocalDate.of(taxYear.getValue, Month.JANUARY.getValue, 2)
       .minusYears(65)
    )
}
