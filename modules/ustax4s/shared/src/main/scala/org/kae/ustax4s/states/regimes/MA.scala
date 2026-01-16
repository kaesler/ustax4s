package org.kae.ustax4s.states.regimes

import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.NonNegMoneys.Deduction
import org.kae.ustax4s.states.MaritalStatus.{Married, Unmarried}
import org.kae.ustax4s.states.ProgressiveStateRegime
import org.kae.ustax4s.states.Syntax.*

object MA
    extends ProgressiveStateRegime(
      rateFunctions = Map(
        Unmarried -> List(
          0         -> 5.0,
          1_083_159 -> 9.0
        ).asRateFunction,
        Married -> List(
          0         -> 5.0,
          1_083_159 -> 9.0
        ).asRateFunction
      ),
      personalExemptions = Map(
        Single          -> Deduction(4000),
        HeadOfHousehold -> Deduction(6800),
        Married         -> Deduction(8800)
      ),
      oldAgeExemption = (age: Int) => if age >= 65 then Deduction(700) else Deduction.zero,
      standardDeductions = _ => Deduction.zero,
      perDependentExemption = Deduction(1000),
      exemptionsAreCredits = false
    ):

end MA
