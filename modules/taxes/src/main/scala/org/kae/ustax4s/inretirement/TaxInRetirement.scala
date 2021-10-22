package org.kae.ustax4s.inretirement

import cats.Show
import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.forms.Form1040
import org.kae.ustax4s.federal.{
  OrdinaryIncomeBrackets,
  QualifiedIncomeBrackets,
  Regime,
  TaxableSocialSecurity,
  Trump
}
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.state.StateTaxMA

/** Simplified interface to 1040 calculations. Assume: No deductions credits or other complications.
  */
object TaxInRetirement:

  final case class FederalTaxResults(
    ssRelevantOtherIncome: Money,
    taxableSocialSecurity: Money,
    personalExceptionDeduction: Money,
    standardDeduction: Money,
    netDeduction: Money,
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
      b.append(s"  personalExceptionDeduction: $personalExceptionDeduction\n")
      b.append(s"  standardDeduction: $standardDeduction\n")
      b.append(s"  netDeduction: $netDeduction\n")
      b.append(s"  taxableOrdinaryIncome: $taxableOrdinaryIncome\n")
      b.append(s"  taxOnOrdinaryIncome: $taxOnOrdinaryIncome\n")
      b.append(s"  taxOnQualifiedIncome: $taxOnQualifiedIncome\n")
      b.append(s"  taxDue: $taxDue\n")

      b.result

  def federalTaxDue(
    regime: Regime,
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    // Self plus dependents
    personalExemptions: Int,
    itemizedDeductions: Money
  ): Money =
    federalTaxResults(
      regime,
      year,
      birthDate,
      filingStatus,
      socSec,
      ordinaryIncomeNonSS,
      qualifiedIncome,
      personalExemptions: Int,
      itemizedDeductions: Money
    ).taxDue.rounded

  def federalTaxResults(
    regime: Regime,
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    // Self plus dependents
    personalExemptions: Int,
    itemizedDeductions: Money
  ): FederalTaxResults =
    val ssRelevantOtherIncome = ordinaryIncomeNonSS + qualifiedIncome
    val taxableSocialSecurity =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = filingStatus,
        socialSecurityBenefits = socSec,
        ssRelevantOtherIncome
      )

    val taxableOrdinaryIncome = (taxableSocialSecurity + ordinaryIncomeNonSS) subp
      regime.netDeduction(year, filingStatus, birthDate, personalExemptions, itemizedDeductions)

    val taxOnOrdinaryIncome = regime
      .ordinaryIncomeBrackets(year, filingStatus)
      .taxDue(taxableOrdinaryIncome)
    val taxOnQualifiedIncome = regime
      .qualifiedIncomeBrackets(year, filingStatus)
      .taxDueFunctionally(
        taxableOrdinaryIncome,
        qualifiedIncome
      )
    FederalTaxResults(
      ssRelevantOtherIncome,
      taxableSocialSecurity,
      regime.personalExemptionDeduction(year, personalExemptions),
      regime.standardDeduction(year, filingStatus, birthDate),
      regime.netDeduction(year, filingStatus, birthDate, personalExemptions, itemizedDeductions),
      taxableOrdinaryIncome,
      taxOnOrdinaryIncome,
      taxOnQualifiedIncome
    )
  end federalTaxResults

  // Note: for tests only
  def federalTaxDueUsingForm1040(
    year: Year,
    birthDate: LocalDate,
    filingStatus: FilingStatus,
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedDividends: Money,
    verbose: Boolean
  ): Money =
    val regime = Trump

    val form = Form1040(
      filingStatus,
      taxableIraDistributions = ordinaryIncomeNonSS,
      socialSecurityBenefits = socSec,
      // The rest not applicable in retirement.
      standardDeduction = regime.standardDeduction(year, filingStatus, birthDate),
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

    Form1040
      .totalFederalTax(
        form,
        regime.ordinaryIncomeBrackets(year, filingStatus),
        regime.qualifiedIncomeBrackets(year, filingStatus)
      )
      .rounded
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
        dependents,
        filingStatus
      )(massachusettsGrossIncome)
      .rounded
