package org.kae.ustax4s.federal

import java.time.{LocalDate, Month, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.*
import scala.annotation.tailrec

object StandardDeduction:

  def of(year: Year, filingStatus: FilingStatus, birthDate: LocalDate): Money =
    unadjustedForAge(year, filingStatus) +
      (if isAge65OrOlder(birthDate, year) then Money(1350) else Money.zero)

  private def unadjustedForAge(year: Year, filingStatus: FilingStatus): Money =
    (year.getValue, filingStatus) match

      case (year, fs) if year > 2025 =>
        val inflationAssumed = 0.02
        unadjustedForAge(Year.of(2017), fs).mul(
          math.pow(1.0 + inflationAssumed, (2021 - 2017).toDouble)
        )

      // An estimate for 2021 as if the Trump tax cuts had not occurred.
      case (year, fs) if year > 2021 && year < 2026 =>
        unadjustedForAge(Year.of(2021), fs)

      // Note: for now assume 2021 rates in later years, and prior to
      // reversion of Trump tax cuts.
      case (year, fs) if year > 2021 && year < 2026 =>
        unadjustedForAge(Year.of(2021), fs)

      case (2021, HeadOfHousehold) => Money(18800)
      case (2020, HeadOfHousehold) => Money(18650)
      case (2019, HeadOfHousehold) => Money(18350)
      case (2018, HeadOfHousehold) => Money(18000)
      case (2017, HeadOfHousehold) => Money(9350)

      case (2021, Single) => Money(12550)
      case (2020, Single) => Money(12400)
      case (2019, Single) => Money(12200)
      case (2018, Single) => Money(12000)
      case (2017, Single) => Money(6350)

      // TODO: account for Trump tax cut expiry in 2026 and later
      case _ => ???

  private def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    birthDate.isBefore(
      LocalDate
        .of(taxYear.getValue, Month.JANUARY.getValue, 2)
        .minusYears(65)
    )
