package org.kae.ustax4s

package forms

final case class Form1040(
  filingStatus: FilingStatus,
  standardDeduction: TMoney,
  schedule1: Option[Schedule1],
  schedule3: Option[Schedule3],
  schedule4: Option[Schedule4],
  schedule5: Option[Schedule5],
  // Line 12:
  // This this year-dependent. Julia must be under 17.
  // So it only applies in 2021.
  childTaxCredit: TMoney = TMoney.zero,
  // Line 1:
  wages: TMoney,
  // Line 2a:
  taxExemptInterest: TMoney,
  // Line 2b:
  taxableInterest: TMoney,
  // Line 3a: Note that this is a subset of ordinary dividends.
  qualifiedDividends: TMoney,
  // Line 3b: Note this includes qualifiedDividends
  ordinaryDividends: TMoney,
  // Line 4b:
  taxableIraDistributions: TMoney,
  // Line 5a:
  socialSecurityBenefits: TMoney,
  rates: TaxRates
) {

  def totalInvestmentIncome: TMoney = {
    // Line 3b:
    ordinaryDividends +
    // Line 6:
    scheduleD
      .map(_.netLongTermCapitalGains)
      .getOrElse(TMoney.zero)
  }

  // This is what gets taxed at LTCG rates.
  def qualifiedInvestmentIncome: TMoney = {
    // Line 3a:
    qualifiedDividends +
    // Line 6:
    scheduleD
      .map(_.netLongTermCapitalGains)
      .getOrElse(TMoney.zero)
  }

  // Line 5b:
  def taxableSocialSecurityBenefits: TMoney =
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      filingStatus = filingStatus,
      socialSecurityBenefits = socialSecurityBenefits,
      relevantIncome = TMoney.sum(
        wages,
        taxableInterest,
        taxExemptInterest,
        taxableIraDistributions,
        ordinaryDividends,
        // This pulls in capital gains.
        schedule1.map(_.additionalIncome).getOrElse(TMoney.zero)
      )
    )

  def scheduleD: Option[ScheduleD] = schedule1.flatMap(_.scheduleD)

  // Line 7b:
  def totalIncome: TMoney =
    TMoney.sum(
      // Line 1
      wages,
      // Line 2b
      taxableInterest,
      // Line 4b
      taxableIraDistributions,
      ordinaryDividends,
      // This pulls in capital gains.
      schedule1.map(_.additionalIncome).getOrElse(TMoney.zero),
      // Line 5b:
      taxableSocialSecurityBenefits
    )

  // Line 7:
  def adjustedGrossIncome: TMoney =
    totalIncome - schedule1.map(_.adjustmentsToIncome).getOrElse(TMoney.zero)

  // Line 10:
  def taxableIncome: TMoney =
    adjustedGrossIncome - standardDeduction

  def taxableOrdinaryIncome: TMoney = taxableIncome - qualifiedInvestmentIncome

  def showValues: String = {
    s"""
       |status: $filingStatus
       |std deduction: ${rates.standardDeduction}
       |taxableSocialSecurityBenefits: $taxableSocialSecurityBenefits
       |qualifiedInvestmentIncome: $qualifiedInvestmentIncome
       |taxableIraDistributions: $taxableIraDistributions
       |totalIncome: $totalIncome
       |adjustedGrossIncome: $adjustedGrossIncome
       |taxableIncome: $taxableIncome
       |taxableOrdinaryIncome: $taxableOrdinaryIncome
       |""".stripMargin
  }
}
