package org.kae.ustax4s.federal.newcalculator

import org.kae.ustax4s.federal
import org.kae.ustax4s.federal.{
  FederalTaxCalculator,
  FederalTaxResults,
  Inflation,
  TaxableSocialSecurity
}
import org.kae.ustax4s.money.Money

final case class RegimeYearStatusPerson(
  regimeYearStatus: RegimeYearStatus,
  person: Person,
  inflationFactor: Double = 1.0
) extends FederalTaxCalculator:
  require(inflationFactor >= 1.0)

  override def federalTaxResults(
    socSec: Money,
    ordinaryIncomeNonSS: Money,
    qualifiedIncome: Money,
    itemizedDeductions: Money
  )(
    // TODO: ignored here for now
    inflation: Option[Inflation]
  ): FederalTaxResults =
    val ssRelevantOtherIncome = ordinaryIncomeNonSS + qualifiedIncome
    val taxableSocialSecurity =
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = filingStatus,
        socialSecurityBenefits = socSec,
        ssRelevantOtherIncome
      )

    val taxableOrdinaryIncome = (taxableSocialSecurity + ordinaryIncomeNonSS) subp
      netDeduction(itemizedDeductions)

    val taxOnOrdinaryIncome = regime
      .ordinaryIncomeBrackets(year, filingStatus)
      .taxDue(taxableOrdinaryIncome)

    val taxOnQualifiedIncome = regime
      .qualifiedIncomeBrackets(year, filingStatus)
      .taxDueFunctionally(
        taxableOrdinaryIncome,
        qualifiedIncome
      )

    federal.FederalTaxResults(
      ssRelevantOtherIncome,
      taxableSocialSecurity,
      personalExemptionDeduction,
      standardDeduction,
      netDeduction(itemizedDeductions),
      taxableOrdinaryIncome,
      taxOnOrdinaryIncome,
      taxOnQualifiedIncome
    )
  end federalTaxResults

  def withInflationFactor(factor: Double): RegimeYearStatusPerson = copy(
    regimeYearStatus = regimeYearStatus.withInflationFactor(factor),
    inflationFactor = factor
  )

  private inline def regime       = regimeYearStatus.regimeYear.regime
  private inline def year         = regimeYearStatus.regimeYear.year
  private inline def filingStatus = regimeYearStatus.filingStatus

  private def standardDeduction: Money =
    regime.standardDeduction(
      year,
      filingStatus,
      person.birthDate
    ) mul inflationFactor

  private def personalExemptionDeduction: Money =
    regime.personalExemptionDeduction(
      year,
      person.personalExemptions
    ) mul inflationFactor

  private def netDeduction(itemizedDeductions: Money): Money =
    Money.max(
      standardDeduction,
      personalExemptionDeduction + itemizedDeductions
    )

end RegimeYearStatusPerson
