package org.kae.ustax4s

import java.time.Year
import org.kae.ustax4s.forms.Form1040

/**
  * Simplified interface to 1040 calcs.
  * Assume:
  *   - The only non-SS income is from 401k or LTCG
  *   - No deductions credits or other complications.
  */
object SimpleTaxInRetirement extends IntMoneySyntax {

  def taxDue(
    year: Year,
    filingStatus: FilingStatus,
    socSec: TMoney,
    incomeFrom401kEtc: TMoney,
    qualifiedInvestmentIncome: TMoney
  ): TMoney = {
    val rates = TaxRates.of(year, filingStatus, Kevin.birthDate)
    val taxableSocialSecurity =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = filingStatus,
        socialSecurityBenefits = socSec,
        relevantIncome = incomeFrom401kEtc + qualifiedInvestmentIncome
      )
    val taxableOrdinaryIncome = (taxableSocialSecurity + incomeFrom401kEtc) - rates.standardDeduction
    val taxOnOrdinaryIncome =
      rates.ordinaryIncomeBrackets.taxDue(taxableOrdinaryIncome)
    val taxOnInvestments = rates.investmentIncomeBrackets.taxDueFunctionally(
      taxableOrdinaryIncome,
      qualifiedInvestmentIncome
    )
    (taxOnInvestments + taxOnOrdinaryIncome).rounded
  }

  def taxDueNoQualifiedInvestments(
    year: Year,
    filingStatus: FilingStatus,
    socSec: TMoney,
    incomeFrom401kEtc: TMoney
  ): TMoney = {
    val rates = TaxRates.of(year, filingStatus, Kevin.birthDate)
    val taxableSocialSecurity =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = filingStatus,
        relevantIncome = incomeFrom401kEtc,
        socialSecurityBenefits = socSec
      )

    // Note the order here:
    //  1.sum all income with SS.
    //  2. subtract standard deduction.
    val taxableIncome = (incomeFrom401kEtc + taxableSocialSecurity) - rates.standardDeduction

    rates.ordinaryIncomeBrackets.taxDue(taxableIncome).rounded
  }

  def taxDueUsingForm1040(
    year: Year,
    filingStatus: FilingStatus,
    socSec: TMoney,
    incomeFrom401k: TMoney,
    qualifiedDividends: TMoney
  ): TMoney = {
    val myRates = TaxRates.of(
      year,
      filingStatus,
      Kevin.birthDate
    )

    val form = Form1040(
      filingStatus,
      rates = myRates,
      taxableIraDistributions = incomeFrom401k,
      socialSecurityBenefits = socSec,
      // The rest not applicable in retirement.
      standardDeduction = myRates.standardDeduction,
      schedule1 = None,
      schedule3 = None,
      schedule4 = None,
      schedule5 = None,
      childTaxCredit = TMoney.zero,
      wages = TMoney.zero,
      taxExemptInterest = TMoney.zero,
      taxableInterest = TMoney.zero,
      qualifiedDividends = qualifiedDividends,
      ordinaryDividends = qualifiedDividends
    )
    // println(form.showValues)
    myRates.totalTax(form).rounded
  }
}
