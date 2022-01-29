package org.kae.ustax4s.federal
package yearly

import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

// TODO some sanity tests for this static data:
//   - correct regime
//   - consistent perPersonExemption
//   - monotonicity by year
//   - monotonicity by FS

final case class YearlyValues(
  regime: Regime,
  perPersonExemption: Deduction,
  unadjustedStandardDeduction: FilingStatus => Deduction,
  adjustmentWhenOver65: Deduction,
  adjustmentWhenOver65AndSingle: Deduction,
  ordinaryBrackets: FilingStatus => OrdinaryBrackets,
  qualifiedBrackets: FilingStatus => QualifiedBrackets
)

object YearlyValues:
  def of(year: Year): Option[YearlyValues] = m.get(year.getValue)

  private val m: Map[Int, YearlyValues] = Map(
    2016 -> Year2016.values,
    2017 -> Year2017.values,
    2018 -> Year2018.values,
    2019 -> Year2019.values,
    2020 -> Year2020.values,
    2021 -> Year2021.values,
    2022 -> Year2022.values
  )

end YearlyValues
