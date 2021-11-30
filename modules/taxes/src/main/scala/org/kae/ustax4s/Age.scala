package org.kae.ustax4s

import cats.implicits.*
import java.time.{LocalDate, Month, Year}

object Age:
  // TODO: duplicate this precise logic in Haskell and PureScript
  // TODO: Unit tests, esp. edge cases.
  // You're considered to be 65 on the day before your 65th birthday.
  // Therefore, you are considered age 65 at the end of the year
  // if your 65th birthday is on or before January 1 of the following year.
  def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    !birthDate.isAfter(
      LocalDate
        .of(taxYear.getValue + 1, Month.JANUARY.getValue, 1)
        .minusYears(65)
    )
