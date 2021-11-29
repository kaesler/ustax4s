package org.kae.ustax4s.federal

import java.time.{LocalDate, Year}
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.{FilingStatus, InflationEstimate}

trait BoundRegime(
  regime: Regime,
  val year: Year,
  filingStatus: FilingStatus,
  birthDate: LocalDate,
  personalExemptions: Int
):
  def unadjustedStandardDeduction: Money
  def adjustmentWhenOver65: Money
  def adjustmentWhenOver65AndSingle: Money

  def perPersonExemption: Money
  def ordinaryIncomeBrackets: OrdinaryIncomeBrackets
  def qualifiedIncomeBrackets: QualifiedIncomeBrackets

  def name: String = regime.name

  // TODO: needs property spec
  final def standardDeduction: Money =
    unadjustedStandardDeduction +
      (
        if Regime.isAge65OrOlder(birthDate, year) then
          adjustmentWhenOver65 +
            (
              if filingStatus.isSingle then adjustmentWhenOver65AndSingle
              else 0
            )
        else 0
      )

  // TODO: needs property spec
  final def personalExemptionDeduction: Money =
    perPersonExemption mul personalExemptions

  // TODO: needs property spec
  // netDed >= all of ped, stdDm, item
  final def netDeduction(itemizedDeductions: Money): Money =
    personalExemptionDeduction +
      Money.max(
        standardDeduction,
        itemizedDeductions
      )

  // TODO: needs property spec
  def calculator: FederalTaxCalculator =
    (
      socSec: Money,
      ordinaryIncomeNonSS: Money,
      qualifiedIncome: Money,
      itemizedDeductions: Money
    ) => {

      val ssRelevantOtherIncome = ordinaryIncomeNonSS + qualifiedIncome

      // Note: this does not currently get adjusted for inflation.
      val taxableSocialSecurity =
        TaxableSocialSecurity.taxableSocialSecurityBenefits(
          filingStatus = filingStatus,
          socialSecurityBenefits = socSec,
          ssRelevantOtherIncome
        )

      val taxableOrdinaryIncome =
        (taxableSocialSecurity + ordinaryIncomeNonSS) subp
          netDeduction(itemizedDeductions)

      val taxOnOrdinaryIncome = ordinaryIncomeBrackets.taxDue(taxableOrdinaryIncome)

      val taxOnQualifiedIncome = qualifiedIncomeBrackets.taxDueFunctionally(
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

      // TODO: restrict what is legal.
      import math.Ordered.orderingToOrdered
      require(estimate.targetFutureYear > base.year)
      regime.failIfInvalid(estimate.targetFutureYear)

      override val name =
        s"${base.name}-estimatedFor-${estimate.targetFutureYear.getValue}"

      // TODO: split this into:
      //   - basic stdDed
      //   - over65Adjustment
      //   - unmarriedAdjustment
      // and inflate each.
      override def unadjustedStandardDeduction: Money =
        base.unadjustedStandardDeduction mul estimate.factor(base.year)
      override def adjustmentWhenOver65: Money =
        base.adjustmentWhenOver65 mul estimate.factor(base.year)
      override def adjustmentWhenOver65AndSingle: Money =
        base.adjustmentWhenOver65AndSingle mul estimate.factor(base.year)

      override val perPersonExemption: Money =
        base.perPersonExemption mul estimate.factor(base.year)

      override val ordinaryIncomeBrackets: OrdinaryIncomeBrackets =
        base.ordinaryIncomeBrackets.inflatedBy(estimate.factor(base.year))

      override val qualifiedIncomeBrackets: QualifiedIncomeBrackets =
        base.qualifiedIncomeBrackets.inflatedBy(estimate.factor(base.year))
    }

end BoundRegime

object BoundRegime:

  def create(
    regime: Regime,
    year: Year,
    filingStatus: FilingStatus,
    birthDate: LocalDate,
    personalExemptions: Int
  ): BoundRegime =
    new BoundRegime(regime, year, filingStatus, birthDate, personalExemptions) {

      override def unadjustedStandardDeduction: Money =
        regime.unadjustedStandardDeduction(this.year, filingStatus)
      override def adjustmentWhenOver65: Money =
        regime.adjustmentWhenOver65(this.year)
      override def adjustmentWhenOver65AndSingle: Money =
        regime.adjustmentWhenOver65AndSingle(this.year)

      override val perPersonExemption: Money = regime.perPersonExemption(this.year)

      override def ordinaryIncomeBrackets: OrdinaryIncomeBrackets =
        regime.ordinaryIncomeBrackets(this.year, filingStatus)

      override def qualifiedIncomeBrackets: QualifiedIncomeBrackets =
        regime.qualifiedIncomeBrackets(this.year, filingStatus)
    }
