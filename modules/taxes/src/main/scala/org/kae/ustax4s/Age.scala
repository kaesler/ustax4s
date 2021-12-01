package org.kae.ustax4s

import cats.implicits.*
import java.time.{LocalDate, Month, Year}

object Age:
  // You're considered by the IRS to be 65 on the day before your 65th birthday.
  // Therefore, you are considered age 65 at the end of the year
  // if your 65th birthday is on or before January 1 of the following year.
  def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    !birthDate.isAfter(
      LocalDate
        .of(taxYear.getValue + 1, Month.JANUARY.getValue, 1)
        .minusYears(65)
    )
