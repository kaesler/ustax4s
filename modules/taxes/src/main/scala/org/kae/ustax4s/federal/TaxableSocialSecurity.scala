package org.kae.ustax4s.federal

import cats.implicits.*
import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*

object TaxableSocialSecurity:

  private val two = 2

  private def bases(filingStatus: FilingStatus): (Money, Money) =
    filingStatus match
      case Single | HeadOfHousehold => (25000, 34000)

  // Adjusted to model the fact that the bases are not adjusted annually
  // as tax brackets are. So we just estimate: amount rises 3% per year
  // but does not exceed 85%.
  def taxableSocialSecurityBenefitsAdjusted(
    filingStatus: FilingStatus,
    socialSecurityBenefits: Money,
    ssRelevantOtherIncome: Money,
    year: Year
  ): Money =
    val unadjusted = taxableSocialSecurityBenefits(
      filingStatus = filingStatus,
      socialSecurityBenefits = socialSecurityBenefits,
      ssRelevantOtherIncome = ssRelevantOtherIncome
    )
    if year.isBefore(Year.of(2022)) then unadjusted
    else
      val adjustmentFactor = 1.0 + ((year.getValue - 2021) * 0.03)
      val adjusted         = unadjusted mul adjustmentFactor
      List(
        adjusted,
        socialSecurityBenefits mul 0.85
      ).min

  def taxableSocialSecurityBenefits(
    filingStatus: FilingStatus,
    socialSecurityBenefits: Money,
    ssRelevantOtherIncome: Money
  ): Money =
    val (lowBase, highBase) = bases(filingStatus)

    val combinedIncome: Money = ssRelevantOtherIncome + (socialSecurityBenefits div two)

    if combinedIncome < lowBase then 0
    else if combinedIncome < highBase then
      val fractionTaxable  = 0.5
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable
      // Half of the amount in this bracket, but no more than 50%
      List(
        (combinedIncome subp lowBase) mul fractionTaxable,
        maxSocSecTaxable
      ).min
    else
      val fractionTaxable  = 0.85
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable

      List(
        // Half in previous bracket and .85 in this bracket,
        // but no more than 0.85 of SS benes.
        Money(4500) + ((combinedIncome subp highBase) mul fractionTaxable),
        maxSocSecTaxable
      ).min
