package org.kae.ustax4s

import eu.timepit.refined._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.numeric.PosDouble
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}

object TaxableSocialSecurity extends IntMoneySyntax {

  private val two = refineMV[Positive](2)

  private def bases(filingStatus: FilingStatus): (TMoney, TMoney) =
    filingStatus match {
      case Single | HeadOfHousehold => (25000.tm, 34000.tm)

      // TODO: MarriedJoint => (32000, 44000)
    }

  def taxableSocialSecurityBenefits(
    filingStatus: FilingStatus,
    relevantIncome: TMoney,
    socialSecurityBenefits: TMoney
  ): TMoney = {
    val (lowBase, highBase) = bases(filingStatus)

    val combinedIncome = relevantIncome + socialSecurityBenefits / two

    if (combinedIncome < lowBase)
      TMoney.zero
    else if (combinedIncome < highBase) {
      val fractionTaxable = PosDouble.unsafeFrom(0.5)
      val maxSocSecTaxable = socialSecurityBenefits mul fractionTaxable
      // Half of the amount in this bracket, but no more than 50%
      TMoney.min(
        (combinedIncome - lowBase) mul fractionTaxable,
        maxSocSecTaxable
      )
    } else {
      val fractionTaxable = PosDouble.unsafeFrom(0.85)
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
