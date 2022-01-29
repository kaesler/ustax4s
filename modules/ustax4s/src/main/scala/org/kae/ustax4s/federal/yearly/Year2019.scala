package org.kae.ustax4s.federal
package yearly

import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.money.Deduction

object Year2019:
  val values: YearlyValues = YearlyValues(
    regime = Trump,
    perPersonExemption = Deduction.zero,
    unadjustedStandardDeduction = ???,
    adjustmentWhenOver65 = ???,
    adjustmentWhenOver65AndSingle = ???,
    ordinaryBrackets = Map(
      HeadOfHousehold -> Map(
      ),
      Single -> Map(
      )
    ).view.mapValues(OrdinaryBrackets.create),
    qualifiedBrackets = Map(
      HeadOfHousehold -> Map(
      ),
      Single -> Map(
      )
    ).view.mapValues(QualifiedBrackets.create)
  )
