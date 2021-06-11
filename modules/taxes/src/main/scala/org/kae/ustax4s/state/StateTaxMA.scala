package org.kae.ustax4s.state

import java.time.{LocalDate, Year}
import org.kae.ustax4s.money.TMoney
import org.kae.ustax4s.money.MoneySyntax.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*

object StateTaxMA:

  // TODO: why necessary?
  import StateTaxRate.given

  def taxDue(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    dependents: Int
  )(
    // Excludes SocSec. So it is
    //  - earned wages
    //  - interest
    //  - dividends
    //  - capital gains
    massachusettsGrossIncome: TMoney
  ): TMoney =
    TMoney.max(
      TMoney.zero,
      massachusettsGrossIncome -
        totalExemptions(year, filingStatus, birthDate, dependents)
    ) taxAt rate(year)

  private def totalExemptions(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    dependents: Int
  ): TMoney =
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
  ): TMoney =
    (year.getValue, filingStatus) match
      // Note: for now assume same for future years as 2020.
      case (year, fs) if year > 2020 =>
        personalExemption(Year.of(2020), fs)

      case (2020, HeadOfHousehold) => TMoney(6800)
      case (2020, Single)          => TMoney(4400)

      case (2019, HeadOfHousehold) => TMoney(6800)
      case (2019, Single)          => TMoney(4400)

      case (2018, HeadOfHousehold) => TMoney(6800)
      case (2018, Single)          => TMoney(4400)

      case _ => ???

  private def age65OrOlderExemption(year: Year, birthDate: LocalDate): TMoney =
    if isAge65OrOlder(birthDate, year) then 700.asMoney
    else TMoney.zero

  private def isAge65OrOlder(birthDate: LocalDate, taxYear: Year): Boolean =
    taxYear.getValue - birthDate.getYear >= 65

  private def dependentExceptions(dependents: Int): TMoney =
    TMoney(1000) mul dependents
