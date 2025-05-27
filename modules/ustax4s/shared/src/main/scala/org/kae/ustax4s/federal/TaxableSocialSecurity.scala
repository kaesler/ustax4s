package org.kae.ustax4s.federal

import java.time.Year
import org.kae.ustax4s.{FilingStatus, SourceLoc}
import org.kae.ustax4s.FilingStatus.*
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.{Income, IncomeThreshold}
import scala.math.Ordering.Implicits.infixOrderingOps

object TaxableSocialSecurity:

  // 3 brackets for combined income:
  //   - low: no SS taxed
  //   - middle: up to half
  private def thresholds(filingStatus: FilingStatus): (IncomeThreshold, IncomeThreshold) =
    filingStatus match
      case Single | HeadOfHousehold =>
        (IncomeThreshold(25000), IncomeThreshold(34000))
      case MarriedJoint =>
        (IncomeThreshold(32000), IncomeThreshold(44000))

  // Current year, current dollars.
  def taxableSocialSecurityBenefits(
    filingStatus: FilingStatus,
    socialSecurityBenefits: Income,
    ssRelevantOtherIncome: Income
  ): Income =
    val (lowThreshold, highThreshold) = thresholds(filingStatus)

    val middleBracketWidth     = highThreshold.absoluteDifference(lowThreshold)
    val combinedIncome: Income = ssRelevantOtherIncome + (socialSecurityBenefits mul 0.5)

    if combinedIncome isBelow lowThreshold then Income.zero
    else if combinedIncome isBelow highThreshold then
      val fractionTaxable  = 0.5
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable
      // Half of the amount in this bracket, but no more than 50%
      List(
        (combinedIncome amountAbove lowThreshold) mul fractionTaxable,
        maxSocSecTaxable
      ).min
    else
      val fractionTaxable  = 0.85
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable

      List(
        // Half in previous bracket and .85 in this bracket,
        // but no more than 0.85 of SS benes.
        (middleBracketWidth divInt 2) +
          ((combinedIncome amountAbove highThreshold) mul fractionTaxable),
        maxSocSecTaxable
      ).min

  // The thresholds used in this computation are not adjusted each year for
  // inflation, as tax brackets are. Social security payments are adjusted.
  // What to do?
  //   - expand the money values by the estimate for net inflation
  //   - compute the taxable amount
  //   - express as a fraction of the inflated SS benefits
  //   - apply it to the current dollar benefit amount

  private def taxableSocialSecurityBenefitsInflated(
    filingStatus: FilingStatus,
    socialSecurityBenefits: Income,
    ssRelevantOtherIncome: Income,
    netInflationFactor: Double
  ): Income =
    require(netInflationFactor >= 1.0, SourceLoc.loc)
    val inflatedSocialSecurityBenefits = socialSecurityBenefits mul netInflationFactor
    val inflatedSsRelevantOtherIncome  = ssRelevantOtherIncome mul netInflationFactor
    val inflatedTaxableAmount = taxableSocialSecurityBenefits(
      filingStatus,
      inflatedSocialSecurityBenefits,
      inflatedSsRelevantOtherIncome
    )
    val fractionTaxable             = inflatedTaxableAmount div inflatedSocialSecurityBenefits
    val amountTaxableInCurrentMoney = socialSecurityBenefits mul fractionTaxable
    amountTaxableInCurrentMoney

  // Helpfully named alias for taxableSocialSecurityBenefits.
  // Inflated income applied to the unchanging thresholds.
  def taxableSocialSecurityBenefitsFutureYearFutureDollars(
    filingStatus: FilingStatus,
    socialSecurityBenefits: Income,
    ssRelevantOtherIncome: Income
  ): Income = taxableSocialSecurityBenefits(
    filingStatus,
    socialSecurityBenefits,
    ssRelevantOtherIncome
  )

  // Future year, benefits as of today.
  def taxableSocialSecurityBenefitsFutureYearCurrentDollars(
    filingStatus: FilingStatus,
    socialSecurityBenefits: Income,
    ssRelevantOtherIncome: Income,
    futureYear: Year,
    estimatedAnnualInflation: Double
  ): Income =
    require(futureYear > YearlyValues.last.year, SourceLoc.loc)
    require(estimatedAnnualInflation >= 0.0, SourceLoc.loc)

    val baseYear = YearlyValues.last.year

    val netInflationFactor = math.pow(
      1.0 + estimatedAnnualInflation,
      (futureYear.getValue - baseYear.getValue).toDouble
    )

    taxableSocialSecurityBenefitsInflated(
      filingStatus,
      socialSecurityBenefits,
      ssRelevantOtherIncome,
      netInflationFactor
    )
