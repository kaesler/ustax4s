package org.kae.ustax4s.state_ma

import cats.implicits.*
import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*
import org.kae.ustax4s.{Age, FilingStatus, NotYetImplemented}
import scala.annotation.tailrec

object StateMATaxCalculator:

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
    massachusettsGrossIncome
      .subp(totalExemptions(year, filingStatus, birthDate, dependents))
      .taxAt(rateFor(year))

  private def totalExemptions(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    dependents: Int
  ): Money =
    List(
      personalExemption(year, filingStatus),
      age65OrOlderExemption(year, birthDate),
      dependentExceptions(dependents)
    ).combineAll

  // Note: Social Security is not taxed.
  private def rateFor(year: Year): StateMATaxRate =
    val r = year.getValue match
      case 2018          => 0.051
      case 2019          => 0.0505
      case 2020          => 0.05
      case x if x > 2020 => 0.05
      case x if x < 2018 => 0.051
    StateMATaxRate.unsafeFrom(r)

  @tailrec
  private def personalExemption(
    year: Year,
    filingStatus: FilingStatus
  ): Money =
    (year.getValue, filingStatus) match
      // Note: for now assume same for future years as 2020.
      case (year, fs) if year > 2020 =>
        personalExemption(Year.of(2020), fs)

      case (2020, HeadOfHousehold) => 6800
      case (2020, Single)          => 4400

      case (2019, HeadOfHousehold) => 6800
      case (2019, Single)          => 4400

      case (2018, HeadOfHousehold) => 6800
      case (2018, Single)          => 4400

      case (2017, HeadOfHousehold) => 6800
      case (2017, Single)          => 4400

      case _ => throw NotYetImplemented(year)

  private def age65OrOlderExemption(year: Year, birthDate: LocalDate): Money =
    if Age.isAge65OrOlder(birthDate, year) then 700
    else 0

  private def dependentExceptions(dependents: Int): Money =
    Money(1000) mul dependents
