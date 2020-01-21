package org.kae.ustax4s

import eu.timepit.refined._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.numeric.PosDouble

object TaxableSocialSecurity {

  private val two = refineMV[Positive](2)

  def taxableSocialSecurityBenefits(
    relevantIncome: TMoney,
    socialSecurityBenefits: TMoney
  ): TMoney = {
    val combinedIncome = relevantIncome + socialSecurityBenefits / two

    // Note: we assume single here.
    // TODO: check this algorithm
    // TODO: write some tests
    if (combinedIncome < TMoney.u(25000))
      TMoney.zero
    else if (combinedIncome < TMoney.u(34000)) {
      val fractionTaxable = PosDouble.unsafeFrom(0.5)
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable
      // Half of the amount in this bracket, but no more than 50%
      TMoney.min(
        (combinedIncome - TMoney.u(25000)) mul fractionTaxable,
        maxSocSecTaxable
      )
    } else {
      val fractionTaxable = PosDouble.unsafeFrom(0.85)
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable
      TMoney.min(
        // Half in previous bracket and .85 in this bracket,
        // but no more than 0.85 of SS benes.
        TMoney.u(9000) +
          (socialSecurityBenefits - TMoney.u(34000)) mul fractionTaxable,
        maxSocSecTaxable
      )
    }
  }
}
