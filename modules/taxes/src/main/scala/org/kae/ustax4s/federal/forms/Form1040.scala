package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.federal.{TaxRates, TaxableSocialSecurity}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.money.*

final case class Form1040(
  filingStatus: FilingStatus,
  standardDeduction: Money,
  schedule1: Option[Schedule1],
  schedule3: Option[Schedule3],
  schedule4: Option[Schedule4],
  schedule5: Option[Schedule5],
  // Line 12:
  // This this year-dependent. Julia must be under 17.
  // So it only applies in 2021.
  childTaxCredit: Money = Money.zero,
  // Line 1:
  wages: Money,
  // Line 2a:
  taxExemptInterest: Money,
  // Line 2b:
  taxableInterest: Money,
  // Line 3a: Note that this is a subset of ordinary dividends.
  qualifiedDividends: Money,
  // Line 3b: Note this includes qualifiedDividends
  ordinaryDividends: Money,
  // Line 4b:
  taxableIraDistributions: Money,
  // Line 5a:
  socialSecurityBenefits: Money,
  rates: TaxRates
):
  def totalInvestmentIncome: Money =
    // Line 3b:
    ordinaryDividends +
      // Line 6:
      scheduleD
        .map(_.netLongTermCapitalGains)
        .getOrElse(Money.zero)

  // This is what gets taxed at LTCG rates.
  def qualifiedIncome: Money =
    // Line 3a:
    qualifiedDividends +
      // Line 6:
      scheduleD
        .map(_.netLongTermCapitalGains)
        .getOrElse(Money.zero)

  // Line 5b:
  def taxableSocialSecurityBenefits: Money =
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      filingStatus = filingStatus,
      socialSecurityBenefits = socialSecurityBenefits,
      ssRelevantOtherIncome = Money.sum(
        wages,
        taxableInterest,
        taxExemptInterest,
        taxableIraDistributions,
        ordinaryDividends,
        // This pulls in capital gains.
        schedule1.map(_.additionalIncome).getOrElse(Money.zero)
      )
    )

  def scheduleD: Option[ScheduleD] = schedule1.flatMap(_.scheduleD)

  // Line 7b:
  def totalIncome: Money =
    Money.sum(
      // Line 1
      wages,
      // Line 2b
      taxableInterest,
      // Line 4b
      taxableIraDistributions,
      ordinaryDividends,
      // This pulls in capital gains.
      schedule1.map(_.additionalIncome).getOrElse(Money.zero),
      // Line 5b:
      taxableSocialSecurityBenefits
    )

  // Line 7:
  def adjustedGrossIncome: Money =
    totalIncome - schedule1.map(_.adjustmentsToIncome).getOrElse(Money.zero)

  // Line 10:
  def taxableIncome: Money =
    adjustedGrossIncome - standardDeduction

  def taxableOrdinaryIncome: Money = taxableIncome - qualifiedIncome

  def showValues: String =
    s"""
       |status: $filingStatus
       |std deduction: ${rates.standardDeduction}
       |taxableSocialSecurityBenefits: $taxableSocialSecurityBenefits
       |qualifiedInvestmentIncome: $qualifiedIncome
       |taxableIraDistributions: $taxableIraDistributions
       |totalIncome: $totalIncome
       |adjustedGrossIncome: $adjustedGrossIncome
       |taxableIncome: $taxableIncome
       |taxableOrdinaryIncome: $taxableOrdinaryIncome
       |""".stripMargin
