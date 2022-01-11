package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.{Income, IncomeThreshold}

object TaxableSocialSecurity:

  private def bases(filingStatus: FilingStatus): (IncomeThreshold, IncomeThreshold) =
    filingStatus match
      case Single | HeadOfHousehold => (IncomeThreshold(25000), IncomeThreshold(34000))

  // Adjusted to model the fact that the bases are not adjusted annually
  // as tax brackets are. So we just estimate: amount rises 3% per year
  // but does not exceed 85%.
  def taxableSocialSecurityBenefitsAdjusted(
    filingStatus: FilingStatus,
    socialSecurityBenefits: Income,
    ssRelevantOtherIncome: Income,
    year: Year
  ): Income =
    val unadjusted = taxableSocialSecurityBenefits(
      filingStatus = filingStatus,
      socialSecurityBenefits = socialSecurityBenefits,
      ssRelevantOtherIncome = ssRelevantOtherIncome
    )
    if year.isBefore(Year.of(2022)) then unadjusted
    else
      val adjustmentFactor = 1.0 + ((year.getValue - 2021) * 0.03)
      val adjusted         = unadjusted inflateBy adjustmentFactor
      List(
        adjusted,
        socialSecurityBenefits mul 0.85
      ).min

  def taxableSocialSecurityBenefits(
    filingStatus: FilingStatus,
    socialSecurityBenefits: Income,
    ssRelevantOtherIncome: Income
  ): Income =
    val (lowBase, highBase) = bases(filingStatus)

    val combinedIncome: Income = ssRelevantOtherIncome + (socialSecurityBenefits mul 0.5)

    if combinedIncome isBelow lowBase then Income.zero
    else if combinedIncome isBelow highBase then
      val fractionTaxable  = 0.5
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable
      // Half of the amount in this bracket, but no more than 50%
      List(
        (combinedIncome amountAbove lowBase) mul fractionTaxable,
        maxSocSecTaxable
      ).min
    else
      val fractionTaxable  = 0.85
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable

      List(
        // Half in previous bracket and .85 in this bracket,
        // but no more than 0.85 of SS benes.
        Income(4500) + ((combinedIncome amountAbove highBase) mul fractionTaxable),
        maxSocSecTaxable
      ).min
