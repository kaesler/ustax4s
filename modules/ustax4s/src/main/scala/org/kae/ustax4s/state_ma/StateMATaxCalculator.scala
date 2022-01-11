package org.kae.ustax4s.state_ma

import cats.implicits.*
import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable}
import org.kae.ustax4s.{Age, FilingStatus, NotYetImplemented}
import scala.util.chaining.*

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
    massachusettsGrossIncome: Income
  ): TaxPayable =
    TaxFunctions.forStateTaxableIncome(rateFor(year))(
      // taxableIncome
      massachusettsGrossIncome
        .applyDeductions(
          totalExemptions(year, filingStatus, birthDate, dependents)
        )
    )

  private def totalExemptions(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    dependents: Int
  ): Deduction =
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

  private def personalExemption(
    year: Year,
    filingStatus: FilingStatus
  ): Deduction =
    (
      (year.getValue, filingStatus) match
        case (2022, HeadOfHousehold) => 6800
        case (2022, Single)          => 4400

        case (2021, HeadOfHousehold) => 6800
        case (2021, Single)          => 4400

        case (2020, HeadOfHousehold) => 6800
        case (2020, Single)          => 4400

        case (2019, HeadOfHousehold) => 6800
        case (2019, Single)          => 4400

        case (2018, HeadOfHousehold) => 6800
        case (2018, Single)          => 4400

        case (2017, HeadOfHousehold) => 6800
        case (2017, Single)          => 4400

        case _ => throw NotYetImplemented(year)
    ).pipe(Deduction.apply)

  private def age65OrOlderExemption(year: Year, birthDate: LocalDate): Deduction =
    Deduction(
      if Age.isAge65OrOlder(birthDate, year) then 700
      else 0
    )

  private def dependentExceptions(dependents: Int): Deduction =
    Deduction(1000) mul dependents