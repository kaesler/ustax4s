package org.kae.ustax4s

package forms

import eu.timepit.refined._
import eu.timepit.refined.numeric.Positive

final case class Form1040(
  standardDeduction: TMoney,
  schedule1: Option[Schedule1],
  schedule3: Option[Schedule3],
  schedule4: Option[Schedule4],
  schedule5: Option[Schedule5],

  // Line 12:
  childTaxCredit: TMoney = TMoney.u(2000),

  // Line 1:
  wages: TMoney,

  // Line 2a:
  taxExemptInterest: TMoney,

  // Line 2b:
  taxableInterest: TMoney,

  // Line 3a:
  qualifiedDividends: TMoney,

  // Line 3b:
  ordinaryDividends: TMoney,

  // Line 4b:
  taxableIras: TMoney,

  // Line 5a:
  socialSecurityBenefits: TMoney,

  rates: TaxRates
) {
  private val two = refineMV[Positive](2)

  // Line 5b:
  def taxableSocialSecurityBenefits: TMoney =
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      TMoney.sum(
        wages,
        taxExemptInterest,
        taxableInterest,
        ordinaryDividends,
        taxableIras,
        schedule1.map(_.additionalIncome).getOrElse(TMoney.zero)
      ),
      socialSecurityBenefits
    )

  def scheduleD: Option[ScheduleD] = schedule1.flatMap(_.scheduleD)

  // This is what gets taxed at LTCG rates.
  def qualifiedInvestmentIncome: TMoney =
    qualifiedDividends + scheduleD.map(_.netLongTermCapitalGains).getOrElse(TMoney.zero)

  // Line 6:
  def totalIncome: TMoney =
    TMoney.sum(
      wages,
      taxableInterest,
      ordinaryDividends,
      taxableIras,
      schedule1.map(_.additionalIncome).getOrElse(TMoney.zero),
      taxableSocialSecurityBenefits
    )

  // Line 7:
  def adjustedGrossIncome: TMoney =
    totalIncome - schedule1.map(_.adjustmentsToIncome).getOrElse(TMoney.zero)

  // Line 10:
  def taxableIncome: TMoney =
    adjustedGrossIncome - standardDeduction

  def taxableOrdinaryIncome: TMoney = taxableIncome - qualifiedInvestmentIncome
}
