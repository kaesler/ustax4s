package org.kae.ustax4s

import eu.timepit.refined._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.numeric.PosDouble
import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

object TaxableSocialSecurity extends IntMoneySyntax {

  private val two = refineMV[Positive](2)

  private def bases(filingStatus: FilingStatus): (TMoney, TMoney) =
    filingStatus match {
      case Single | HeadOfHousehold => (25000.tm, 34000.tm)
    }

  // Adjusted to model the fact that the bases are not adjusted annually
  // as tax brackets are. So we just estimate: amount rises 3% per year
  // but does not exceed 85%.
  def taxableSocialSecurityBenefitsAdjusted(
    filingStatus: FilingStatus,
    socialSecurityBenefits: TMoney,
    ssRelevantOtherIncome: TMoney,
    year: Year
  ): TMoney = {
    val unadjusted = taxableSocialSecurityBenefits(
      filingStatus = filingStatus,
      socialSecurityBenefits = socialSecurityBenefits,
      ssRelevantOtherIncome = ssRelevantOtherIncome
    )
    if (year.isBefore(Year.of(2022)))
      unadjusted
    else {
      val adjustmentFactor = 1.0 + ((year.getValue - 2021) * 0.03)
      val adjusted         = unadjusted mul PosDouble.unsafeFrom(adjustmentFactor)
      TMoney.min(
        adjusted,
        socialSecurityBenefits mul PosDouble.unsafeFrom(0.85)
      )
      unadjusted mul PosDouble.unsafeFrom(adjustmentFactor)
    }
  }

  def taxableSocialSecurityBenefits(
    filingStatus: FilingStatus,
    socialSecurityBenefits: TMoney,
    ssRelevantOtherIncome: TMoney
  ): TMoney = {
    val (lowBase, highBase) = bases(filingStatus)

    val combinedIncome = ssRelevantOtherIncome + socialSecurityBenefits / two

    if (combinedIncome < lowBase)
      TMoney.zero
    else if (combinedIncome < highBase) {
      val fractionTaxable  = PosDouble.unsafeFrom(0.5)
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable
      // Half of the amount in this bracket, but no more than 50%
      TMoney.min(
        (combinedIncome - lowBase) mul fractionTaxable,
        maxSocSecTaxable
      )
    } else {
      val fractionTaxable  = PosDouble.unsafeFrom(0.85)
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable

      TMoney.min(
        // Half in previous bracket and .85 in this bracket,
        // but no more than 0.85 of SS benes.
        TMoney.u(4500) + ((combinedIncome - highBase) mul fractionTaxable),
        maxSocSecTaxable
      )
    }
  }
}
