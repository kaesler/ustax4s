package org.kae.ustax4s.states.regimes

import cats.syntax.all.*
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.RateFunction.RateFunction
import org.kae.ustax4s.federal.FedCalcResults
import org.kae.ustax4s.money.NonNegMoneys.{Deduction, Income, RefundableTaxCredit, TaxCredit}
import org.kae.ustax4s.states.*
import org.kae.ustax4s.states.MaritalStatus.{Married, Unmarried}
import org.kae.ustax4s.states.Syntax.*
import org.kae.ustax4s.{Age, FilingStatus}

object MA extends ProgressiveStateRegime:

  override val rateFunctions: MaritalStatus => RateFunction[StateTaxRate] =
    Map(
      Unmarried -> List(
        0         -> 5.0,
        1_083_159 -> 9.0
      ).asRateFunction,
      Married -> List(
        0         -> 5.0,
        1_083_159 -> 9.0
      ).asRateFunction
    )
  end rateFunctions

  override val personalExemptions: FilingStatus => Deduction =
    Map(
      Single          -> Deduction(4000),
      HeadOfHousehold -> Deduction(6800),
      Married         -> Deduction(8800)
    )
  end personalExemptions

  override val exemptionForAge: Int => Deduction =
    (age: Int) => if age >= 65 then Deduction(700) else Deduction.zero

  override val standardDeductions: FilingStatus => Deduction =
    Function.const(Deduction.zero)

  override val perDependentExemption: Deduction = Deduction(1000)

  override val exemptionsAreCredits: Boolean = false

  override def computeStateGrossIncome(fr: FedCalcResults): Income =
    // TODO: also exclude pensions not taxed by the state
    fr.agi monus fr.incomeScenario.socSec

  override def computeStateDeductions(
    fr: FedCalcResults,
    props: StatePersonProperties
  ): Deduction =
    List(
      personalExemptions(fr.filingStatus),
      exemptionForAge(Age.ageAtEndOfYear(fr.birthDate, fr.year)),
      perDependentExemption mul props.stateQualifiedDependents
    ).combineAll
  end computeStateDeductions

  override def computeStateCredits(
    fr: FedCalcResults,
    props: StatePersonProperties
  ): TaxCredit =
    // TODO
    TaxCredit.zero

  override def computeStateRefundableCredits(
    fr: FedCalcResults,
    props: StatePersonProperties
  ): RefundableTaxCredit =
    // TODO
    RefundableTaxCredit.zero

end MA
