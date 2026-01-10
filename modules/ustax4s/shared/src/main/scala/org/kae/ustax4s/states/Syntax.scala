package org.kae.ustax4s.states

import org.kae.ustax4s.RateFunction
import org.kae.ustax4s.money.NonNegMoneys.IncomeThreshold

object Syntax:

  extension (pairs: List[(threshold: Int, percentage: Double)])
    def asRateFunction: RateFunction[StateTaxRate] =
      RateFunction.of(
        pairs.map: pair =>
          (IncomeThreshold(pair.threshold), StateTaxRate.unsafeFrom(pair.percentage / 100.0))
      )
  end extension

end Syntax
