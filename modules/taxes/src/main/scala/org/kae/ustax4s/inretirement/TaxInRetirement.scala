package org.kae.ustax4s
package inretirement

import cats.Show
import java.time.{LocalDate, Year}
import org.kae.ustax4s.forms.Form1040

/** Simplified interface to 1040 calculations. Assume: No deductions credits or other complications.
  */
object TaxInRetirement extends IntMoneySyntax:

  final case class FederalTaxResults(
    ssRelevantOtherIncome: TMoney,
    taxableSocialSecurity: TMoney,
    standardDeduction: TMoney,
    taxableOrdinaryIncome: TMoney,
    taxOnOrdinaryIncome: TMoney,
    taxOnQualifiedIncome: TMoney
  ):
    def taxDue: TMoney = taxOnOrdinaryIncome + taxOnQualifiedIncome

  object FederalTaxResults:
    given Show[FederalTaxResults] = (r: FederalTaxResults) =>
      val b = StringBuilder()
      b.append("Outputs\n")
      import r.*
      b.append(s"  ssRelevantOtherIncome: $ssRelevantOtherIncome\n")
      b.append(s"  taxableSocSec: $taxableSocialSecurity\n")
      b.append(s"  standardDeduction: $standardDeduction\n")
      b.append(s"  taxableOrdinaryIncome: $taxableOrdinaryIncome\n")
      b.append(s"  taxOnOrdinaryIncome: $taxOnOrdinaryIncome\n")
      b.append(s"  taxOnQualifiedIncome: $taxOnQualifiedIncome\n")
      b.append(s"  taxDue: $taxDue\n")

      b.result

  def federalTaxDue(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: TMoney,
    ordinaryIncomeNonSS: TMoney,
    qualifiedIncome: TMoney
  ): TMoney =
    federalTaxResults(
      year,
      birthDate,
      filingStatus,
      socSec,
      ordinaryIncomeNonSS,
      qualifiedIncome
    ).taxDue.rounded

  def federalTaxResults(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: TMoney,
    ordinaryIncomeNonSS: TMoney,
    qualifiedIncome: TMoney
  ): FederalTaxResults =
    val ssRelevantOtherIncome = ordinaryIncomeNonSS + qualifiedIncome
    val taxableSocialSecurity =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = filingStatus,
        socialSecurityBenefits = socSec,
        ssRelevantOtherIncome
      )

    val rates = TaxRates.of(year, filingStatus, birthDate)
    val taxableOrdinaryIncome = (taxableSocialSecurity + ordinaryIncomeNonSS) -
      rates.standardDeduction

    val taxOnOrdinaryIncome =
      rates.ordinaryIncomeBrackets.taxDue(taxableOrdinaryIncome)
    val taxOnQualifiedIncome = rates.qualifiedIncomeBrackets.taxDueFunctionally(
      taxableOrdinaryIncome,
      qualifiedIncome
    )
    FederalTaxResults(
      ssRelevantOtherIncome,
      taxableSocialSecurity,
      rates.standardDeduction,
      taxableOrdinaryIncome,
      taxOnOrdinaryIncome,
      taxOnQualifiedIncome
    )
  end federalTaxResults

  def federalTaxDueUsingForm1040(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: TMoney,
    ordinaryIncomeNonSS: TMoney,
    qualifiedDividends: TMoney,
    verbose: Boolean
  ): TMoney =
    val myRates = TaxRates.of(
      year,
      filingStatus,
      birthDate
    )

    val form = Form1040(
      filingStatus,
      rates = myRates,
      taxableIraDistributions = ordinaryIncomeNonSS,
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
    if verbose then println(form.showValues)

    myRates.totalTax(form).rounded
  end federalTaxDueUsingForm1040

  def stateTaxDue(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    dependents: Int,
    // Excludes SocSec. So it is
    //  - earned wages
    //  - interest
    //  - dividends
    //  - capital gains
    massachusettsGrossIncome: TMoney
  ): TMoney =
    StateTaxMA
      .taxDue(
        year,
        birthDate,
        filingStatus,
        dependents
      )(massachusettsGrossIncome)
      .rounded
