package org.kae.ustax4s.federal

import cats.syntax.all.*
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable, TaxableIncome}

trait FedTaxCalculator() extends (FederalCalcInput => FedCalcResults)

object FedTaxCalculator:

  def from(br: BoundFedRegime): FedTaxCalculator =
    new FedTaxCalculator():
      private val thisCalculator = this

      override def apply(input: FederalCalcInput): FedCalcResults =
        new FedCalcResults:
          import input.*

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
            FedTaxFunctions.taxPayableOnOrdinaryIncome(br.ordinaryRateFunction)(taxableOrdinaryIncome)

          override lazy val taxOnQualifiedIncome: TaxPayable =
            FedTaxFunctions.taxPayableOnQualifiedIncome(br.qualifiedRateFunction)(
              taxableOrdinaryIncome,
              qualifiedIncome
            )

          // TODO: compute the AOTC tax credit

          // Note: we center the deltas over X value.
          private val delta = 200

          override lazy val taxSlopeForOrdinaryIncome: Double =
            val deltaX    = Income(delta)
            val halfDelta = deltaX.divInt(2)
            val highY     = thisCalculator(input.withMoreOrdinaryIncome(halfDelta)).taxPayable
            val lowY      = thisCalculator(input.withLessOrdinaryIncome(halfDelta)).taxPayable
            highY.reduceBy(lowY).asDouble / deltaX.asDouble
          end taxSlopeForOrdinaryIncome

          override lazy val taxSlopeForQualifiedIncome: Double =
            val deltaX    = TaxableIncome(delta)
            val halfDelta = TaxableIncome(delta / 2)
            val highY     = thisCalculator(input.withMoreQualifiedIncome(halfDelta)).taxPayable
            val lowY      = thisCalculator(input.withLessQualifiedIncome(halfDelta)).taxPayable
            highY.reduceBy(lowY).asDouble / deltaX.asDouble
          end taxSlopeForQualifiedIncome

          override lazy val taxSlopeForSocSec: Double =
            val deltaX    = Income(delta)
            val halfDelta = deltaX.divInt(2)
            val highY     = thisCalculator(input.withMoreSocSec(halfDelta)).taxPayable
            val lowY      = thisCalculator(input.withLessSocSec(halfDelta)).taxPayable
            highY.reduceBy(lowY).asDouble / deltaX.asDouble
          end taxSlopeForSocSec

        end new
      end apply
    end new
  end from
end FedTaxCalculator
