package org.kae.ustax4s.federal

import cats.syntax.all.*
import java.time.LocalDate
import org.kae.ustax4s.IncomeScenario
import org.kae.ustax4s.calculators.FedAndStateCalcResults
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable, TaxableIncome}
import org.kae.ustax4s.states.State
import scala.annotation.unused

trait FedCalculator(
) extends (IncomeScenario => FedCalcResults):
  def birthDate: LocalDate
  def personalExemptions: Int

  def apply(state: State): IncomeScenario => FedAndStateCalcResults
end FedCalculator

object FedCalculator:

  def from(
    br: BoundFedRegime,
    _birthDate: LocalDate,
    _personalExemptions: Int
  ): FedCalculator =
    new FedCalculator():
      override val birthDate: LocalDate    = _birthDate
      override val personalExemptions: Int = _personalExemptions

      private val thisCalculator = this

      override def apply(state: State): IncomeScenario => FedAndStateCalcResults =
        ??? // TODO

      override def apply(scenario: IncomeScenario): FedCalcResults =
        new FedCalcResults:
          import scenario.*

          // Note: this does not currently get adjusted for inflation.
          override lazy val taxableSocialSecurity: Income =
            br.taxableSocialSecurityBenefits(
              socialSecurityBenefits = socSec,
              ssRelevantOtherIncome = List(
                ordinaryIncomeNonSS,
                qualifiedIncome
              ).combineAll
            )

          override lazy val agi: Income = List(
            ordinaryIncomeNonSS,
            qualifiedIncome,
            taxableSocialSecurity
          ).combineAll

          override lazy val personalExemptionDeduction: Deduction =
            br.personalExemptionDeduction(personalExemptions)

          override def standardDeduction: Deduction =
            br.netDeduction(agi, birthDate, personalExemptions, itemizedDeductions)

          override def meansTestedSeniorDeduction: Deduction =
            br.meansTestedSeniorDeduction(agi, birthDate)

          override lazy val netDeduction: Deduction =
            br.netDeduction(agi, birthDate, personalExemptions, itemizedDeductions)

          override lazy val taxableOrdinaryIncome: TaxableIncome =
            List(
              taxableSocialSecurity,
              ordinaryIncomeNonSS
            ).combineAll
              .applyDeductions(netDeduction)

          override lazy val taxOnOrdinaryIncome: TaxPayable =
            FedTaxFunctions.taxPayableOnOrdinaryIncome(br.ordinaryRateFunction)(
              taxableOrdinaryIncome
            )

          override lazy val taxOnQualifiedIncome: TaxPayable =
            FedTaxFunctions.taxPayableOnQualifiedIncome(br.qualifiedRateFunction)(
              taxableOrdinaryIncome,
              qualifiedIncome
            )

          // TODO: compute the AOTC refundable tax credit
          // THis needs to be offset against net tax payable so far
          @unused
          private val netTaxPayableBeforeCredits: TaxPayable =
            taxOnOrdinaryIncome + taxOnQualifiedIncome

          // Note: we center the deltas over X value.
          private val delta = 200

          override lazy val taxSlopeForOrdinaryIncome: Double =
            val deltaX    = Income(delta)
            val halfDelta = deltaX.divInt(2)
            val highY     = thisCalculator(scenario.withMoreOrdinaryIncome(halfDelta)).taxPayable
            val lowY      = thisCalculator(scenario.withLessOrdinaryIncome(halfDelta)).taxPayable
            highY.reduceBy(lowY).asDouble / deltaX.asDouble
          end taxSlopeForOrdinaryIncome

          override lazy val taxSlopeForQualifiedIncome: Double =
            val deltaX    = TaxableIncome(delta)
            val halfDelta = TaxableIncome(delta / 2)
            val highY     = thisCalculator(scenario.withMoreQualifiedIncome(halfDelta)).taxPayable
            val lowY      = thisCalculator(scenario.withLessQualifiedIncome(halfDelta)).taxPayable
            highY.reduceBy(lowY).asDouble / deltaX.asDouble
          end taxSlopeForQualifiedIncome

          override lazy val taxSlopeForSocSec: Double =
            val deltaX    = Income(delta)
            val halfDelta = deltaX.divInt(2)
            val highY     = thisCalculator(scenario.withMoreSocSec(halfDelta)).taxPayable
            val lowY      = thisCalculator(scenario.withLessSocSec(halfDelta)).taxPayable
            highY.reduceBy(lowY).asDouble / deltaX.asDouble
          end taxSlopeForSocSec

        end new
      end apply
    end new
  end from
end FedCalculator
