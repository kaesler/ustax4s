package org.kae.ustax4s.federal.yearly

import org.kae.ustax4s.RateFunction
import org.kae.ustax4s.federal.{FederalTaxRate, OrdinaryRateFunction, QualifiedRateFunction}
import org.kae.ustax4s.money.NonNegMoneys.IncomeThreshold

private[yearly] object Syntax:

  extension (pairs: List[(threshold: Int, percentage: Double)])
    def asOrdinaryRateFunction: OrdinaryRateFunction =
      OrdinaryRateFunction(
        RateFunction.of(
          pairs.map: pair =>
            (IncomeThreshold(pair.threshold), FederalTaxRate.unsafeFrom(pair.percentage / 100.0))
        )
      )
    end asOrdinaryRateFunction

    def asQualifiedRateFunction: QualifiedRateFunction =
      QualifiedRateFunction(
        RateFunction.of(
          pairs.map: pair =>
            (IncomeThreshold(pair.threshold), FederalTaxRate.unsafeFrom(pair.percentage / 100.0))
        )
      )
    end asQualifiedRateFunction

end Syntax
