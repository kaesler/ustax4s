package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold

object StandardDeduction {
  def of (year: Year, filingStatus: FilingStatus): TMoney =
    (year.getValue, filingStatus) match {
      case (2018, HeadOfHousehold) => TMoney.u(18_000)
      case _ => ???
    }
}
