package org.kae.ustax4s.federal

import cats.Show
import cats.implicits.*
import java.time.{LocalDate, Year}
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.*
import org.kae.ustax4s.taxfunction.TaxFunction
import org.kae.ustax4s.{Age, FilingStatus, InflationEstimate}
import scala.math.Ordering.Implicits.infixOrderingOps

trait BoundRegime(
  val regime: Regime,
  val year: Year,
  val filingStatus: FilingStatus

  // val birthDate: BirthDate
  // val personalExemptions: Int
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

  // TODO: needs property spec
  def calculator: FederalTaxCalculator =
    (
      birthDate: LocalDate,
      personalExemptions: Int,
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
          .applyDeductions(netDeduction(birthDate, personalExemptions, itemizedDeductions))

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
    }

  def withEstimatedNetInflationFactor(
    targetFutureYear: Year,
    netInflationFactor: Double
  ): BoundRegime =
    require(netInflationFactor >= 1)
    val base = this
    new BoundRegime(
      regime,
      targetFutureYear,
      filingStatus
    ) {
      require(targetFutureYear > YearlyValues.last.year)

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
    }

end BoundRegime

object BoundRegime:

  given Show[BoundRegime] with
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

  def forFutureYear(
    regime: Regime,
    year: Year,
    estimatedAnnualInflationFactor: Double,
    filingStatus: FilingStatus
  ): BoundRegime =
    require(year > YearlyValues.last.year)
    val baseValues         = YearlyValues.mostRecentFor(regime)
    val baseYear           = baseValues.year.getValue
    val yearsWithInflation = (baseYear + 1).to(year.getValue).map(Year.of)
    val inflationFactors = yearsWithInflation
      .map { year =>
        YearlyValues.averageThresholdChangeOverPrevious(year) match
          // Use known inflation for each year where we have it...
          case Some(knownFactor) => knownFactor
          // ...otherwise use estimate.
          case _ => estimatedAnnualInflationFactor

      }
    val netInflationFactor = inflationFactors.product
    forKnownYear(baseValues.year, filingStatus)
      .withEstimatedNetInflationFactor(year, netInflationFactor)

  def forKnownYear(
    year: Year,
    filingStatus: FilingStatus
  ): BoundRegime =
    val yv = YearlyValues.of(year).get

    new BoundRegime(yv.regime, year, filingStatus) {

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
    }
