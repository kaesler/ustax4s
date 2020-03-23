package org.kae.ustax4s

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus.HeadOfHousehold

object StateTaxMA {

  val rate = 0.051

  private def personalExemption(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate
  ): TMoney = {
    val unadjustedForAge = (year.getValue, filingStatus) match {
      case (2020, HeadOfHousehold) => TMoney.u(6800)
      case (2019, HeadOfHousehold) => TMoney.u(6800)
    }
    ???
  }

  // rate
}
