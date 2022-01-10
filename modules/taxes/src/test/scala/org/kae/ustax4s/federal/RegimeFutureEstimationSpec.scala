package org.kae.ustax4s.federal

import java.time.{LocalDate, Year}
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.InflationEstimate

class RegimeFutureEstimationSpec extends FunSuite:
  import math.Ordering.Implicits.infixOrderingOps
  import org.kae.ustax4s.MoneyConversions.given

  private val birthDate: LocalDate = LocalDate.of(1955, 10, 2)

  test("BoundRegime.futureEstimated should work as expected") {
    val baseYear = Year.of(2017)
    val before = BoundRegime.create(
      PreTrump,
      baseYear,
      birthDate,
      HeadOfHousehold,
      2
    )
    val estimate = InflationEstimate(Year.of(2027), 0.03)

    val after = before.futureEstimated(estimate)
    assert(
      before.standardDeduction < after.standardDeduction
    )
    assert(
      before.netDeduction(4000) < after.netDeduction(4000)
    )
    assert(
      after.perPersonExemption == (before.perPersonExemption inflateBy estimate.factor(baseYear))
    )
  }
end RegimeFutureEstimationSpec
