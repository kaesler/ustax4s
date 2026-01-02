package org.kae.ustax4s.federal

import cats.syntax.all.*
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable, TaxableIncome}

trait FederalTaxCalculator() extends (FederalCalcInput => FederalCalcResults)

object FederalTaxCalculator:

  def from(br: BoundRegime): FederalTaxCalculator =
    new FederalTaxCalculator():
      private val thisCalculator = this

      override def apply(input: FederalCalcInput): FederalCalcResults =
        new FederalCalcResults:
          import input.*

          private val ssRelevantOtherIncome =
            List(ordinaryIncomeNonSS, qualifiedIncome).combineAll

          // Note: this does not currently get adjusted for inflation.
          override lazy val taxableSocialSecurity: Income =
            TaxableSocialSecurity.taxableSocialSecurityBenefits(
              filingStatus = br.filingStatus,
              socialSecurityBenefits = socSec,
              ssRelevantOtherIncome
            )

          override lazy val agi: Income = List(
            ordinaryIncomeNonSS,
            qualifiedIncome,
            taxableSocialSecurity
          ).combineAll

          override lazy val taxableOrdinaryIncome: TaxableIncome =
            List(taxableSocialSecurity, ordinaryIncomeNonSS).combineAll
              .applyDeductions(netDeduction)

          override lazy val taxOnOrdinaryIncome: TaxPayable =
            TaxFunctions.taxDueOnOrdinaryIncome(br.ordinaryRateFunction)(taxableOrdinaryIncome)

          override lazy val taxOnQualifiedIncome: TaxPayable =
            TaxFunctions.taxDueOnQualifiedIncome(br.qualifiedRateFunction)(
              taxableOrdinaryIncome,
              qualifiedIncome
            )

          // TODO: compute the AOTC tax credit

          override lazy val personalExemptionDeduction: Deduction =
            br.personalExemptionDeduction(personalExemptions)

          override def standardDeduction: Deduction =
            br.netDeduction(agi, birthDate, personalExemptions, itemizedDeductions)

          override def meansTestedSeniorDeduction: Deduction =
            br.meansTestedSeniorDeduction(agi, birthDate)

          override lazy val netDeduction: Deduction =
            br.netDeduction(agi, birthDate, personalExemptions, itemizedDeductions)

          // Note: we center the deltas over X value.
          private val delta = 200

          override lazy val taxSlopeForOrdinaryIncome: Double =
            val deltaX    = Income(delta)
            val halfDelta = deltaX.divInt(2)
            val highY     = thisCalculator(input.withMoreOrdinaryIncome(halfDelta)).taxDue
            val lowY      = thisCalculator(input.withLessOrdinaryIncome(halfDelta)).taxDue
            highY.reduceBy(lowY).asDouble / deltaX.asDouble
          end taxSlopeForOrdinaryIncome

          override lazy val taxSlopeForQualifiedIncome: Double =
            val deltaX    = TaxableIncome(delta)
            val halfDelta = TaxableIncome(delta / 2)
            val highY     = thisCalculator(input.withMoreQualifiedIncome(halfDelta)).taxDue
            val lowY      = thisCalculator(input.withLessQualifiedIncome(halfDelta)).taxDue
            highY.reduceBy(lowY).asDouble / deltaX.asDouble
          end taxSlopeForQualifiedIncome

          override lazy val taxSlopeForSocSec: Double =
            val deltaX    = Income(delta)
            val halfDelta = deltaX.divInt(2)
            val highY     = thisCalculator(input.withMoreSocSec(halfDelta)).taxDue
            val lowY      = thisCalculator(input.withLessSocSec(halfDelta)).taxDue
            highY.reduceBy(lowY).asDouble / deltaX.asDouble
          end taxSlopeForSocSec

        end new
      end apply
    end new
  end from
end FederalTaxCalculator
