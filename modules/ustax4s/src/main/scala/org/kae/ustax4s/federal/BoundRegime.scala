package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.{LocalDate, Year}
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.*
import org.kae.ustax4s.taxfunction.TaxFunction
import org.kae.ustax4s.{Age, FilingStatus, InflationEstimate}
import scala.math.Ordering.Implicits.infixOrderingOps

trait BoundRegime(
  regime: Regime,
  val year: Year,
  filingStatus: FilingStatus,
  birthDate: LocalDate,
  personalExemptions: Int
):
  def unadjustedStandardDeduction: Deduction
  def adjustmentWhenOver65: Deduction
  def adjustmentWhenOver65AndSingle: Deduction

  def perPersonExemption: Deduction
  def ordinaryBrackets: OrdinaryBrackets
  def qualifiedBrackets: QualifiedBrackets

  def name: String = regime.show

  // TODO: needs property spec
  final def standardDeduction: Deduction =
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

  // TODO: needs property spec
  final def personalExemptionDeduction: Deduction =
    perPersonExemption mul personalExemptions

  // TODO: needs property spec
  // netDed >= all of ped, stdDm, item
  final def netDeduction(itemizedDeductions: Deduction): Deduction =
    personalExemptionDeduction +
      List(
        standardDeduction,
        itemizedDeductions
      ).max

  // TODO: needs property spec
  def calculator: FederalTaxCalculator =
    (
      socSec: Income,
      ordinaryIncomeNonSS: Income,
      qualifiedIncome: TaxableIncome,
      itemizedDeductions: Deduction
    ) => {

      val ssRelevantOtherIncome =
        List(ordinaryIncomeNonSS, qualifiedIncome).combineAll

      // Note: this does not currently get adjusted for inflation.
      val taxableSocialSecurity =
        TaxableSocialSecurity.taxableSocialSecurityBenefits(
          filingStatus = filingStatus,
          socialSecurityBenefits = socSec,
          ssRelevantOtherIncome
        )

      val taxableOrdinaryIncome =
        List(taxableSocialSecurity, ordinaryIncomeNonSS)
          .combineAll
          .applyDeductions(netDeduction(itemizedDeductions))

      val taxOnOrdinaryIncome =
        TaxFunctions.taxDueOnOrdinaryIncome(ordinaryBrackets)(taxableOrdinaryIncome)

      val taxOnQualifiedIncome =
        TaxFunctions.taxDueOnQualifiedIncome(qualifiedBrackets)(
          taxableOrdinaryIncome,
          qualifiedIncome
        )
      FederalTaxResults(
        ssRelevantOtherIncome,
        taxableSocialSecurity,
        personalExemptionDeduction,
        unadjustedStandardDeduction,
        adjustmentWhenOver65,
        adjustmentWhenOver65AndSingle,
        standardDeduction,
        netDeduction(itemizedDeductions),
        taxableOrdinaryIncome,
        taxOnOrdinaryIncome,
        taxOnQualifiedIncome
      )
    }

  // Create a new BoundRegime that behaves like the original but with
  // appropriate adjustments for inflation estimate.
  def futureEstimated(estimate: InflationEstimate): BoundRegime =
    val base = this
    new BoundRegime(
      regime,
      estimate.targetFutureYear,
      filingStatus,
      birthDate,
      personalExemptions
    ) {
      require(estimate.targetFutureYear > YearlyValues.last.year)

      override val name =
        s"${base.name}-estimatedFor-${estimate.targetFutureYear.getValue}"

      override def unadjustedStandardDeduction: Deduction =
        base.unadjustedStandardDeduction inflateBy estimate.factor(base.year)

      override def adjustmentWhenOver65: Deduction =
        base.adjustmentWhenOver65 inflateBy estimate.factor(base.year)

      override def adjustmentWhenOver65AndSingle: Deduction =
        base.adjustmentWhenOver65AndSingle inflateBy estimate.factor(base.year)

      override val perPersonExemption: Deduction =
        base.perPersonExemption inflateBy estimate.factor(base.year)

      override val ordinaryBrackets: OrdinaryBrackets =
        base.ordinaryBrackets.inflatedBy(estimate.factor(base.year))

      override val qualifiedBrackets: QualifiedBrackets =
        base.qualifiedBrackets.inflatedBy(estimate.factor(base.year))
    }

  def withEstimatedNetInflationFactor(
    targetFutureYear: Year,
    netInflationFactor: Double
  ): BoundRegime =
    val base = this
    new BoundRegime(
      regime,
      targetFutureYear,
      filingStatus,
      birthDate,
      personalExemptions
    ) {
      require(targetFutureYear > YearlyValues.last.year)

      override val name =
        s"${base.name}-estimatedFor-${targetFutureYear.getValue}"

      override def unadjustedStandardDeduction: Deduction =
        base.unadjustedStandardDeduction inflateBy netInflationFactor

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
    }

end BoundRegime

object BoundRegime:

  def forFutureYear(
    regime: Regime,
    year: Year,
    estimatedAnnualInflationFactor: Double,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    personalExemptions: Int
  ): BoundRegime =
    require(year > YearlyValues.last.year)
    val baseValues         = YearlyValues.lastFor(regime)
    val baseYear           = baseValues.year.getValue
    val yearsWithInflation = (baseYear + 1).to(year.getValue).map(Year.of)
    val inflationFactors = yearsWithInflation
      .map { year =>
        YearlyValues.averageThresholdChangeOverPrevious(year) match
          case Some(knownFactor) => knownFactor
          case _                 => estimatedAnnualInflationFactor

      }
    val netInflationFactor = inflationFactors.product
    forKnownYear(baseValues.year, birthDate, filingStatus, personalExemptions)
      .withEstimatedNetInflationFactor(year, netInflationFactor)

  def forKnownYear(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    personalExemptions: Int
  ): BoundRegime =
    val yv = YearlyValues.of(year).get

    new BoundRegime(yv.regime, year, filingStatus, birthDate, personalExemptions) {

      override def unadjustedStandardDeduction: Deduction =
        yv.unadjustedStandardDeduction(filingStatus)

      override def adjustmentWhenOver65: Deduction = yv.adjustmentWhenOver65

      override def adjustmentWhenOver65AndSingle: Deduction =
        yv.adjustmentWhenOver65AndSingle

      override val perPersonExemption: Deduction = yv.perPersonExemption

      override def ordinaryBrackets: OrdinaryBrackets =
        yv.ordinaryBrackets(filingStatus)

      override def qualifiedBrackets: QualifiedBrackets =
        yv.qualifiedBrackets(filingStatus)
    }
