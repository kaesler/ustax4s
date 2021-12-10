package org.kae.ustax4s.federal.forms

import cats.implicits.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.{
  OrdinaryIncomeBrackets,
  QualifiedIncomeBrackets,
  TaxableSocialSecurity
}
import org.kae.ustax4s.money.Money

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
  socialSecurityBenefits: Money
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
      ssRelevantOtherIncome = List[Money](
        wages,
        taxableInterest,
        taxExemptInterest,
        taxableIraDistributions,
        ordinaryDividends,
        // This pulls in capital gains.
        schedule1.map(_.additionalIncome).getOrElse(Money.zero)
      ).combineAll
    )

  def scheduleD: Option[ScheduleD] = schedule1.flatMap(_.scheduleD)

  // Line 7b:
  def totalIncome: Money =
    List[Money](
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
    ).combineAll

  // Line 7:
  def adjustedGrossIncome: Money =
    totalIncome subp schedule1.map(_.adjustmentsToIncome).getOrElse(Money.zero)

  // Line 10:
  def taxableIncome: Money =
    adjustedGrossIncome subp standardDeduction

  def taxableOrdinaryIncome: Money = taxableIncome subp qualifiedIncome

  def showValues: String =
    s"""
       |status: $filingStatus
       |std deduction: $standardDeduction
       |taxableSocialSecurityBenefits: $taxableSocialSecurityBenefits
       |qualifiedInvestmentIncome: $qualifiedIncome
       |taxableIraDistributions: $taxableIraDistributions
       |totalIncome: $totalIncome
       |adjustedGrossIncome: $adjustedGrossIncome
       |taxableIncome: $taxableIncome
       |taxableOrdinaryIncome: $taxableOrdinaryIncome
       |""".stripMargin

end Form1040

object Form1040:

  def totalFederalTax(
    form: Form1040,
    ordinaryIncomeBrackets: OrdinaryIncomeBrackets,
    qualifiedIncomeBrackets: QualifiedIncomeBrackets
  ): Money =
    taxDueBeforeCredits(
      form.taxableOrdinaryIncome,
      form.qualifiedIncome,
      ordinaryIncomeBrackets,
      qualifiedIncomeBrackets
    ) +
      form.schedule4.map(_.totalOtherTaxes).getOrElse(Money.zero) subp
      (form.childTaxCredit + form.schedule3
        .map(_.nonRefundableCredits)
        .getOrElse(Money.zero))

  // Line 11:
  def taxDueBeforeCredits(
    ordinaryIncome: Money,
    qualifiedIncome: Money,
    ordinaryIncomeBrackets: OrdinaryIncomeBrackets,
    qualifiedIncomeBrackets: QualifiedIncomeBrackets
  ): Money =
    ordinaryIncomeBrackets.taxDue(ordinaryIncome) +
      qualifiedIncomeBrackets.taxDue(
        ordinaryIncome,
        qualifiedIncome
      )

end Form1040
