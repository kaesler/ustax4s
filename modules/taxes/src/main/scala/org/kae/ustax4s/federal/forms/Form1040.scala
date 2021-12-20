package org.kae.ustax4s.federal.forms

import cats.implicits.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.{
  OrdinaryIncomeBrackets,
  QualifiedIncomeBrackets,
  TaxableSocialSecurity
}
import org.kae.ustax4s.money.*
import org.kae.ustax4s.taxfunction.TaxFunction

final case class Form1040(
  filingStatus: FilingStatus,
  standardDeduction: Deduction,
  schedule1: Option[Schedule1],
  schedule3: Option[Schedule3],
  schedule4: Option[Schedule4],
  schedule5: Option[Schedule5],
  // Line 12:
  // This this year-dependent. Julia must be under 17.
  // So it only applies in 2021.
  childTaxCredit: TaxCredit = TaxCredit.zero,
  // Line 1:
  wages: Income,
  // Line 2a:
  taxExemptInterest: Income,
  // Line 2b:
  taxableInterest: Income,
  // Line 3a: Note that this is a subset of ordinary dividends.
  qualifiedDividends: Income,
  // Line 3b: Note this includes qualifiedDividends
  ordinaryDividends: Income,
  // Line 4b:
  taxableIraDistributions: Income,
  // Line 5a:
  socialSecurityBenefits: Income
):
  def totalInvestmentIncome: Income =
    // Line 3b:
    ordinaryDividends +
      // Line 6:
      scheduleD
        .map(_.netLongTermCapitalGains)
        .getOrElse(Income.zero)

  // This is what gets taxed at LTCG rates.
  def qualifiedIncome: Income =
    // Line 3a:
    qualifiedDividends +
      // Line 6:
      scheduleD
        .map(_.netLongTermCapitalGains)
        .getOrElse(Income.zero)

  // Line 5b:
  def taxableSocialSecurityBenefits: Income =
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      filingStatus = filingStatus,
      socialSecurityBenefits = socialSecurityBenefits,
      ssRelevantOtherIncome = List[Income](
        wages,
        taxableInterest,
        taxExemptInterest,
        taxableIraDistributions,
        ordinaryDividends,
        // This pulls in capital gains.
        schedule1.map(_.additionalIncome).getOrElse(Income.zero)
      ).combineAll
    )

  def scheduleD: Option[ScheduleD] = schedule1.flatMap(_.scheduleD)

  // Line 7b:
  def totalIncome: Income =
    List[Income](
      // Line 1
      wages,
      // Line 2b
      taxableInterest,
      // Line 4b
      taxableIraDistributions,
      ordinaryDividends,
      // This pulls in capital gains.
      schedule1.map(_.additionalIncome).getOrElse(Income.zero),
      // Line 5b:
      taxableSocialSecurityBenefits
    ).combineAll

  // Line 7:
  def adjustedGrossIncome: Income =
    totalIncome applyDeductions schedule1.map(_.adjustmentsToIncome).getOrElse(Deduction.zero)

  // Line 10:
  def taxableIncome: Income =
    adjustedGrossIncome applyDeductions standardDeduction

  def taxableOrdinaryIncome: Income = taxableIncome reduceBy qualifiedIncome

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
  ): TaxPayable =
    taxDueBeforeCredits(
      form.taxableOrdinaryIncome,
      form.qualifiedIncome,
      ordinaryIncomeBrackets,
      qualifiedIncomeBrackets
    ) +
      form.schedule4.map(_.totalOtherTaxes).getOrElse(TaxPayable.zero) applyCredits
      (form.childTaxCredit +
        form.schedule3
          .map(_.nonRefundableCredits)
          .getOrElse(TaxCredit.zero))

  // Line 11:
  def taxDueBeforeCredits(
    ordinaryIncome: Income,
    qualifiedIncome: Income,
    ordinaryIncomeBrackets: OrdinaryIncomeBrackets,
    qualifiedIncomeBrackets: QualifiedIncomeBrackets
  ): TaxPayable =
    TaxFunction.fromBrackets(ordinaryIncomeBrackets.thresholds)(ordinaryIncome) +
      qualifiedIncomeBrackets.taxDue(
        ordinaryIncome,
        qualifiedIncome
      )

end Form1040
