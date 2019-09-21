package org.kae.ustax4s

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

object Kevin {
  val birthDate = LocalDate.of(1955, 10, 2)

  def filingStatus(year: Year): FilingStatus = {
    if (year.getValue <= 2027)
      HeadOfHousehold
    else
      Single
  }
}
