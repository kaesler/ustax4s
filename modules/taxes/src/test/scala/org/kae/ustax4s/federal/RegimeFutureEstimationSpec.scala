package org.kae.ustax4s.federal

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.InflationEstimate
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.Money

class RegimeFutureEstimationSpec extends FunSuite:

  test("BoundRegime.futureEstimated should work as expected") {
    val baseYear = Year.of(2017)
    val before = BoundRegime.create(
      NonTrump,
      baseYear,
      HeadOfHousehold,
      Kevin.birthDate,
      Kevin.personalExemptions(baseYear)
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
      after.perPersonExemption == (before.perPersonExemption mul estimate.factor(baseYear))
    )
  }
end RegimeFutureEstimationSpec
