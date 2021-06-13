package org.kae.ustax4s.state

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*

object StateTaxMA:

  def taxDue(
    year: Year,
    birthDate: LocalDate,
    dependents: Int,
    filingStatus: FilingStatus
  )(
    // Excludes SocSec. So it is
    //  - earned wages
    //  - interest
    //  - dividends
    //  - capital gains
    massachusettsGrossIncome: Money
  ): Money =
    (massachusettsGrossIncome subp
      totalExemptions(year, filingStatus, birthDate, dependents)) taxAt rate(year)

  private def totalExemptions(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    dependents: Int
  ): Money =
    personalExemption(year, filingStatus) +
      age65OrOlderExemption(year, birthDate) +
      dependentExceptions(dependents)

  // Note: Social Security is not taxed.
  private def rate(year: Year): StateTaxRate =
    val r = year.getValue match
      case 2018          => 0.051
      case 2019          => 0.0505
      case 2020          => 0.05
      case x if x > 2020 => 0.05
      case x if x < 2018 => 0.051
    StateTaxRate.unsafeFrom(r)

  private def personalExemption(
    year: Year,
    filingStatus: FilingStatus
  ): Money =
    (year.getValue, filingStatus) match
      // Note: for now assume same for future years as 2020.
      case (year, fs) if year > 2020 =>
        personalExemption(Year.of(2020), fs)

      case (2020, HeadOfHousehold) => Money(6800)
      case (2020, Single)          => Money(4400)

      case (2019, HeadOfHousehold) => Money(6800)
      case (2019, Single)          => Money(4400)

      case (2018, HeadOfHousehold) => Money(6800)
      case (2018, Single)          => Money(4400)

      case _ => ???

  private def age65OrOlderExemption(year: Year, birthDate: LocalDate): Money =
    if isAge65OrOlder(birthDate, year) then 700.asMoney
    else Money.zero

  private def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    taxYear.getValue - birthDate.getYear >= 65

  private def dependentExceptions(dependents: Int): Money =
    Money(1000) mul dependents
