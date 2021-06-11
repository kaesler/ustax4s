package org.kae.ustax4s.inretirement

import cats.Show
import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.forms.Form1040
import org.kae.ustax4s.federal.{TaxRates, TaxableSocialSecurity}
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.state.StateTaxMA

/** Simplified interface to 1040 calculations. Assume: No deductions credits or other complications.
  */
object TaxInRetirement:

  final case class FederalTaxResults(
    ssRelevantOtherIncome: Money,
    taxableSocialSecurity: Money,
    standardDeduction: Money,
    taxableOrdinaryIncome: Money,
    taxOnOrdinaryIncome: Money,
    taxOnQualifiedIncome: Money
  ):
    def taxDue: Money = taxOnOrdinaryIncome + taxOnQualifiedIncome

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
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money
  ): Money =
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
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money
  ): FederalTaxResults =
    val ssRelevantOtherIncome = ordinaryIncomeNonSS + qualifiedIncome
    val taxableSocialSecurity =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = filingStatus,
        socialSecurityBenefits = socSec,
        ssRelevantOtherIncome
      )

    val rates = TaxRates.of(year, filingStatus, birthDate)
    val taxableOrdinaryIncome = (taxableSocialSecurity + ordinaryIncomeNonSS) subp
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
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedDividends: Money,
    verbose: Boolean
  ): Money =
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
      childTaxCredit = Money.zero,
      wages = Money.zero,
      taxExemptInterest = Money.zero,
      taxableInterest = Money.zero,
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
    massachusettsGrossIncome: Money
  ): Money =
    StateTaxMA
      .taxDue(
        year,
        birthDate,
        filingStatus,
        dependents
      )(massachusettsGrossIncome)
      .rounded
