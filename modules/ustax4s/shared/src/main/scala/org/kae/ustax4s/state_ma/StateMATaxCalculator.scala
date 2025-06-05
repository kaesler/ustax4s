package org.kae.ustax4s.state_ma

import cats.implicits.*
import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable}
import org.kae.ustax4s.{Age, FilingStatus}
import scala.util.chaining.*

object StateMATaxCalculator:

  def taxDue(
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    dependents: Int
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
  end taxDue

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
  end totalExemptions

  // Note: Social Security is not taxed.
  private def rateFor(year: Year): StateMATaxRate =
    val r = year.getValue match
      case 2018          => 0.051
      case 2019          => 0.0505
      case 2020          => 0.05
      case x if x > 2020 => 0.05
      case x if x < 2018 => 0.051
    StateMATaxRate.unsafeFrom(r)
  end rateFor

  private def personalExemption(
    year: Year,
    filingStatus: FilingStatus
  ): Deduction =
    (
      (year.getValue, filingStatus) match
        case (2022, MarriedJoint)    => 8800
        case (2022, HeadOfHousehold) => 6800
        case (2022, Single)          => 4400

        case (2021, MarriedJoint)    => 8800
        case (2021, HeadOfHousehold) => 6800
        case (2021, Single)          => 4400

        case (2020, MarriedJoint)    => 8800
        case (2020, HeadOfHousehold) => 6800
        case (2020, Single)          => 4400

        case (2019, MarriedJoint)    => 8800
        case (2019, HeadOfHousehold) => 6800
        case (2019, Single)          => 4400

        case (2018, MarriedJoint)    => 8800
        case (2018, HeadOfHousehold) => 6800
        case (2018, Single)          => 4400

        case (2017, MarriedJoint)    => 8800
        case (2017, HeadOfHousehold) => 6800
        case (2017, Single)          => 4400

        case (2016, MarriedJoint)    => 8800
        case (2016, HeadOfHousehold) => 6800
        case (2016, Single)          => 4400

        // TODO: for now don't inflate state exemptions.
        case (_, MarriedJoint)    => 8800
        case (_, HeadOfHousehold) => 6800
        case (_, Single)          => 4400
    ).pipe(Deduction.apply)
  end personalExemption

  private def age65OrOlderExemption(year: Year, birthDate: LocalDate): Deduction =
    Deduction(
      if Age.isAge65OrOlder(birthDate, year) then 700
      else 0
    )
  end age65OrOlderExemption

  private def dependentExceptions(dependents: Int): Deduction =
    Deduction(1000) mul dependents

end StateMATaxCalculator
