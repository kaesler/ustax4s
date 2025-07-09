package org.kae.ustax4s.federal

import cats.Show
import cats.implicits.*
import java.time.{LocalDate, Year}
import org.kae.ustax4s.calculator.FedTaxResults2
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.*
import org.kae.ustax4s.{Age, FilingStatus, SourceLoc}
import scala.annotation.unused
import scala.math.Ordering.Implicits.infixOrderingOps

trait BoundRegime(
  val regime: Regime,
  val year: Year,
  val filingStatus: FilingStatus
):
  def unadjustedStandardDeduction: Deduction
  def adjustmentWhenOver65: Deduction
  def adjustmentWhenOver65AndSingle: Deduction

  def perPersonExemption: Deduction
  def ordinaryBrackets: OrdinaryBrackets
  def qualifiedBrackets: QualifiedBrackets

  def name: String = regime.show

  // TODO: needs property spec
  final def standardDeduction(birthDate: LocalDate): Deduction =
    unadjustedStandardDeduction +
      (
        if Age.isAge65OrOlder(birthDate, year) then
          adjustmentWhenOver65 +
            (
              if filingStatus.isSingle then adjustmentWhenOver65AndSingle
              else Deduction.zero
            )
        else Deduction.zero
      )
  end standardDeduction

  // TODO: needs property spec
  private final def personalExemptionDeduction(personalExemptions: Int): Deduction =
    perPersonExemption mul personalExemptions

  // TODO: needs property spec
  // netDed >= all of ped, stdDm, item
  final def netDeduction(
    birthDate: LocalDate,
    personalExemptions: Int,
    itemizedDeductions: Deduction
  ): Deduction =
    personalExemptionDeduction(personalExemptions) +
      List(
        standardDeduction(birthDate),
        itemizedDeductions
      ).max
  end netDeduction

  // TODO: needs property spec
  final def calculator: FederalTaxCalculator =
    new FederalTaxCalculator:
      @unused
      def fedResults2(
        birthDate: LocalDate,
        personalExemptions: Int,
        socSec: Income,
        ordinaryIncomeNonSS: Income,
        qualifiedIncome: TaxableIncome,
        itemizedDeductions: Deduction
      ): FedTaxResults2 =
        new FedTaxResults2 {
          private val br: BoundRegime       = BoundRegime.this
          private val ssRelevantOtherIncome =
            List(ordinaryIncomeNonSS, qualifiedIncome).combineAll

          // Note: this does not currently get adjusted for inflation.
          override lazy val taxableSocialSecurity: Income =
            TaxableSocialSecurity.taxableSocialSecurityBenefits(
              filingStatus = filingStatus,
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
            TaxFunctions.taxDueOnOrdinaryIncome(ordinaryBrackets)(taxableOrdinaryIncome)

          override lazy val taxOnQualifiedIncome: TaxPayable =
            TaxFunctions.taxDueOnQualifiedIncome(qualifiedBrackets)(
              taxableOrdinaryIncome,
              qualifiedIncome
            )

          override lazy val personalExemptionDeduction: Deduction =
            br.personalExemptionDeduction(personalExemptions)

          override def standardDeduction: Deduction =
            br.netDeduction(birthDate, personalExemptions, itemizedDeductions)

          // TODO: Implement this.
          // TODO: Include into netDeduction.
          override def meansTestedSeniorDeduction: Deduction = Deduction.zero

          override lazy val netDeduction: Deduction =
            br.netDeduction(birthDate, personalExemptions, itemizedDeductions)

          // TODO: maybe center the delta over the tax due?
          override def taxSlopeForOrdinaryIncome(delta: Income): Double = ???

          // TODO: maybe center the delta over the tax due?
          override def taxSlopeForQualifiedIncome(delta: TaxableIncome): Double = ???
        }

      override def federalTaxResults(
        birthDate: LocalDate,
        personalExemptions: Int,
        socSec: Income,
        ordinaryIncomeNonSS: Income,
        qualifiedIncome: TaxableIncome,
        itemizedDeductions: Deduction
      ): FederalTaxResults =

        // Computed from inputs
        val ssRelevantOtherIncome =
          List(ordinaryIncomeNonSS, qualifiedIncome).combineAll

        // Note: this does not currently get adjusted for inflation.
        val taxableSocialSecurity: Income =
          TaxableSocialSecurity.taxableSocialSecurityBenefits(
            filingStatus = filingStatus,
            socialSecurityBenefits = socSec,
            ssRelevantOtherIncome
          )

        val taxableOrdinaryIncome: TaxableIncome =
          List(taxableSocialSecurity, ordinaryIncomeNonSS).combineAll
            .applyDeductions(netDeduction(birthDate, personalExemptions, itemizedDeductions))

        val taxOnOrdinaryIncome: TaxPayable =
          TaxFunctions.taxDueOnOrdinaryIncome(ordinaryBrackets)(taxableOrdinaryIncome)

        val taxOnQualifiedIncome =
          TaxFunctions.taxDueOnQualifiedIncome(qualifiedBrackets)(
            taxableOrdinaryIncome,
            qualifiedIncome
          )
        FederalTaxResults(
          ssRelevantOtherIncome,
          taxableSocialSecurity,
          personalExemptionDeduction(personalExemptions),
          unadjustedStandardDeduction,
          adjustmentWhenOver65,
          adjustmentWhenOver65AndSingle,
          standardDeduction(birthDate),
          netDeduction(birthDate, personalExemptions, itemizedDeductions),
          taxableOrdinaryIncome,
          taxOnOrdinaryIncome,
          taxOnQualifiedIncome
        )
    end new
  end calculator

  def withEstimatedNetInflationFactor(
    targetFutureYear: Year,
    netInflationFactor: Double
  ): BoundRegime =
    require(netInflationFactor >= 1, SourceLoc())
    val base = this
    new BoundRegime(
      regime,
      targetFutureYear,
      filingStatus
    ):
      require(targetFutureYear > YearlyValues.last.year, SourceLoc())

      override val name =
        s"${base.name}-estimatedFor-${targetFutureYear.getValue}"

      override def unadjustedStandardDeduction: Deduction =
        base.unadjustedStandardDeduction.inflateBy(netInflationFactor)

      override def adjustmentWhenOver65: Deduction =
        base.adjustmentWhenOver65 inflateBy netInflationFactor

      override def adjustmentWhenOver65AndSingle: Deduction =
        base.adjustmentWhenOver65AndSingle inflateBy netInflationFactor

      override val perPersonExemption: Deduction =
        base.perPersonExemption inflateBy netInflationFactor

      override val ordinaryBrackets: OrdinaryBrackets =
        base.ordinaryBrackets.inflatedBy(netInflationFactor)

      override val qualifiedBrackets: QualifiedBrackets =
        base.qualifiedBrackets.inflatedBy(netInflationFactor)
    end new

  end withEstimatedNetInflationFactor

end BoundRegime

object BoundRegime:

  given Show[BoundRegime]:
    def show(r: BoundRegime): String =
      "BoundRegime:\n" ++
        s"  regime: ${r.regime.show}\n" ++
        s"  year: ${r.year}\n" ++
        s"  filingStatus: ${r.filingStatus.show}\n" ++
        s"  unadjustedStandardDeduction: ${r.unadjustedStandardDeduction}\n" ++
        s"  adjustmentWhenOver65: ${r.adjustmentWhenOver65}\n" ++
        s"  adjustmentWhenOver65AnSingle: ${r.adjustmentWhenOver65AndSingle}\n" ++
        s"  ordinaryBrackets: ${r.ordinaryBrackets.show}\n" ++
        s"  qualifiedBrackets: ${r.qualifiedBrackets.show}\n"
  end given

  def forFutureYear(
    regime: Regime,
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus
  ): BoundRegime =
    require(year > YearlyValues.last.year, SourceLoc())
    val baseValues         = YearlyValues.mostRecentFor(regime)
    val baseYear           = baseValues.year.getValue
    val yearsWithInflation = (baseYear + 1).to(year.getValue).map(Year.of)
    val inflationFactors   = yearsWithInflation
      .map: year =>
        YearlyValues.averageThresholdChangeOverPrevious(year) match
          // Use known inflation for each year where we have it...
          case Some(knownFactor) => knownFactor
          // ...otherwise use estimate.
          case _ => estimatedAnnualInflationFactor

    val netInflationFactor = inflationFactors.product
    forKnownYear(baseValues.year, filingStatus)
      .withEstimatedNetInflationFactor(year, netInflationFactor)
  end forFutureYear

  def forKnownYear(
    year: Year,
    filingStatus: FilingStatus
  ): BoundRegime =
    val yv = YearlyValues.of(year).get

    new BoundRegime(yv.regime, year, filingStatus):

      override def unadjustedStandardDeduction: Deduction =
        yv.unadjustedStandardDeduction(this.filingStatus)

      override def adjustmentWhenOver65: Deduction = yv.adjustmentWhenOver65

      override def adjustmentWhenOver65AndSingle: Deduction =
        yv.adjustmentWhenOver65AndSingle

      override val perPersonExemption: Deduction = yv.perPersonExemption

      override def ordinaryBrackets: OrdinaryBrackets =
        yv.ordinaryBrackets(this.filingStatus)

      override def qualifiedBrackets: QualifiedBrackets =
        yv.qualifiedBrackets(this.filingStatus)
    end new
  end forKnownYear
end BoundRegime
