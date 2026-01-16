package org.kae.ustax4s.federal.forms

import cats.implicits.*
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.{OrdinaryRateFunction, QualifiedRateFunction, FedTaxFunctions, TaxableSocialSecurity}
import org.kae.ustax4s.money.*

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
  qualifiedDividends: TaxableIncome,
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
  def qualifiedIncome: TaxableIncome =
    // Line 3a:
    qualifiedDividends +
      // Line 6:
      scheduleD
        .map(_.netLongTermCapitalGains)
        .getOrElse(TaxableIncome.zero)

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
  end taxableSocialSecurityBenefits

  def scheduleD: Option[ScheduleD] = schedule1.flatMap(_.scheduleD)

  // Line 7b:
  private def totalIncome: Income =
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
  end totalIncome

  // Line 7:
  def adjustedGrossIncome: Income =
    totalIncome applyAdjustments schedule1.map(_.adjustmentsToIncome).getOrElse(Deduction.zero)

  // Line 10:
  def taxableIncome: TaxableIncome =
    adjustedGrossIncome applyDeductions standardDeduction

  def taxableOrdinaryIncome: TaxableIncome =
    taxableIncome reduceBy qualifiedIncome

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
  end showValues

end Form1040

object Form1040:

  def totalFederalTax(
    form: Form1040,
    ordinaryIncomeBrackets: OrdinaryRateFunction,
    qualifiedIncomeBrackets: QualifiedRateFunction
  ): TaxPayable =
    taxDueBeforeCredits(
      form.taxableOrdinaryIncome,
      form.qualifiedIncome,
      ordinaryIncomeBrackets,
      qualifiedIncomeBrackets
    ) +
      form.schedule4.map(_.totalOtherTaxes).getOrElse(TaxPayable.zero) applyNonRefundableCredits
      (form.childTaxCredit +
        form.schedule3
          .map(_.nonRefundableCredits)
          .getOrElse(TaxCredit.zero))
  end totalFederalTax

  // Line 11:
  def taxDueBeforeCredits(
    ordinaryIncome: TaxableIncome,
    qualifiedIncome: TaxableIncome,
    ordinaryIncomeBrackets: OrdinaryRateFunction,
    qualifiedIncomeBrackets: QualifiedRateFunction
  ): TaxPayable =
    FedTaxFunctions.taxPayableOnOrdinaryIncome(ordinaryIncomeBrackets)(ordinaryIncome) +
      FedTaxFunctions.taxPayableOnQualifiedIncome(qualifiedIncomeBrackets)(
        ordinaryIncome,
        qualifiedIncome
      )
  end taxDueBeforeCredits

end Form1040
