package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.{LocalDate, Year}
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.InflationEstimate

class BoundRegimeFutureEstimationSpec extends FunSuite:
  import math.Ordering.Implicits.infixOrderingOps
  import org.kae.ustax4s.money.MoneyConversions.given

  private val birthDate: LocalDate = LocalDate.of(1955, 10, 2)

  test("BoundRegime.futureEstimated should behave monotonically") {
    val baseYear = Year.of(2017)
    val before = BoundRegime.forKnownYear(
      baseYear,
      birthDate,
      HeadOfHousehold,
      2
    )
    val estimate = InflationEstimate(Year.of(2027), 0.03)

    val after = before.futureEstimated(estimate)
    assert(before.standardDeduction < after.standardDeduction)
    assert(before.netDeduction(4000) < after.netDeduction(4000))
    assert(
      after.perPersonExemption == (before.perPersonExemption inflateBy estimate.factor(baseYear))
    )
    assert(before.ordinaryBrackets <= after.ordinaryBrackets)
    assert(before.qualifiedBrackets <= after.qualifiedBrackets)
  }
end BoundRegimeFutureEstimationSpec
