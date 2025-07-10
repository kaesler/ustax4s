package org.kae.ustax4s.federal

import cats.Show
import cats.implicits.*
import java.time.{LocalDate, Year}
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.*
import org.kae.ustax4s.{Age, FilingStatus, SourceLoc}
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

  def meansTestedSeniorDeduction(
    agi: Income,
    birthDate: LocalDate
  ): Deduction =
    // Note: this deduction is temporary for 4 years and
    // only applies to those over 65.
    if (2025 to 2028).contains(year.getValue) &&
      Age.isAge65OrOlder(birthDate, year)
    then
      val fullAmount    = 6000.0
      val phaseOutStart = if filingStatus.isSingle then 75000.0 else 150000.0
      val phaseOutRange = 100000.0
      val phaseOutEnd   = phaseOutStart + phaseOutRange
      val phaseOutRate  = fullAmount / phaseOutRange

      if agi <= Income(phaseOutStart) then Deduction(fullAmount)
      else if agi >= Income(phaseOutEnd) then Deduction.zero
      else
        Deduction(
          fullAmount -
            agi
              .reduceBy(Income(phaseOutStart))
              .mul(phaseOutRate)
              .asDouble
        )
    else Deduction.zero
  end meansTestedSeniorDeduction

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
  final def personalExemptionDeduction(personalExemptions: Int): Deduction =
    perPersonExemption mul personalExemptions

  // TODO: needs property spec
  // netDed >= all of ped, stdDm, item
  final def netDeduction(
    agi: Income,
    birthDate: LocalDate,
    personalExemptions: Int,
    itemizedDeductions: Deduction
  ): Deduction =
    personalExemptionDeduction(personalExemptions) +
      List(
        standardDeduction(birthDate),
        itemizedDeductions
      ).max +
      meansTestedSeniorDeduction(agi, birthDate)
  end netDeduction

  // TODO: needs property spec
  final def calculator: FederalTaxCalculator = FederalTaxCalculator.from(this)

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
