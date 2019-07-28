package org.kae.ustax4s

import eu.timepit.refined.auto._

object Form1040 {

  def totalIncome(
      wages: TMoney,
      taxableInterest: TMoney,
      ordinaryDividends: TMoney,
      taxableIras: TMoney,
      taxableSocialSecurityBenefits: TMoney
  ): TMoney =
    TMoney.sum(
      wages,
      taxableInterest,
      ordinaryDividends,
      taxableIras,
      taxableSocialSecurityBenefits)

  def adjustedGrossIncome(
      wages: TMoney,
      taxableInterest: TMoney,
      ordinaryDividends: TMoney,
      taxableIras: TMoney,
      taxableSocialSecurityBenefits: TMoney,
      adjustMentsToIncome: TMoney = TMoney.zero
  ): TMoney = {
    totalIncome(
      wages,
      taxableInterest,
      ordinaryDividends,
      taxableIras,
      taxableSocialSecurityBenefits
    ) subtract adjustMentsToIncome
  }

  def taxableIncome(
    wages: TMoney,
    taxableInterest: TMoney,
    ordinaryDividends: TMoney,
    taxableIras: TMoney,
    taxableSocialSecurityBenefits: TMoney,
    rates: TaxRates
  ): TMoney = {
    adjustedGrossIncome(
      wages,
      taxableInterest,
      ordinaryDividends,
      taxableIras,
      taxableSocialSecurityBenefits).subtract(rates.standardDeduction)
  }

  def totalTax(
    wages: TMoney,
    taxableInterest: TMoney,
    ordinaryDividends: TMoney,
    taxableIras: TMoney,
    taxableSocialSecurityBenefits: TMoney,
    rates: TaxRates
  ): TMoney =
    rates.brackets.taxDue(
      taxableIncome(
        wages,
        taxableInterest,
        ordinaryDividends,
        taxableIras,
        taxableSocialSecurityBenefits,
        rates
      )
    )
}
