package org.kae.ustax4s.states

import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.{FilingStatus, RateFunction}
import org.kae.ustax4s.federal.{FederalCalcInput, FederalCalcResults}
import org.kae.ustax4s.money.Moneys.{Deduction, IncomeThreshold, TaxPayable}
import org.kae.ustax4s.states.MaritalStatus.{Married, Unmarried}
import scala.annotation.unused

// Model this: https://docs.google.com/spreadsheets/d/1I_OuA6uuAs7YoZRc02atHCCxpXaGDn9N/edit?gid=201796956#gid=201796956

// TODO:
//  - MA detqils
//  - std deduction COLA
//  - brackets COLA
//(a) Local income taxes are excluded. Ten states have county- or city-level income taxes; the average effective rates expressed as a percentage of AGI within each jurisdiction (data for 2022, the latest year available, come from the IRS and U.S. Census Bureau) are: AL--0.09%; IN--0.49%; IA--0.09%; KY--1.31%; MD--2.51%; MI--0.18%; MO--0.21%; NY--1.68%; OH--1.49%; PA--1.07%. In CA, CO, DE, KS, NJ, OR, and WV some jurisdictions have payroll taxes, flat-rate wage taxes, or interest and dividend income taxes. See Jared Walczak, Janelle Fritts, and Maxwell James, “Local Income Taxes: A Primer,” Tax Foundation, February 23, 2023, https://taxfoundation.org/local-income-taxes-2023/. (b) These states allow some or all of federal income tax paid to be deducted from state taxable income.
//(c) For single taxpayers with AGI not exceeding $25,999, the standard deduction is $3,000. This standard deduction amount is reduced by $25 for every additional $500 of AGI, not to fall below $2,500. For Married Filing Joint (MFJ) taxpayers with AGI not exceeding $25,999, the standard deduction is $8,500. This standard deduction amount is reduced by $175 for every additional $500 of AGI, not to fall below $5,000. For all taxpayers with AGI of $50,000 or less and claiming a dependent, the dependent exemption is $1,000. This amount is reduced to $500 per dependent for taxpayers with AGI above $50,000 and equal to or less than $100,000. For taxpayers with more than $100,000 in AGI, the dependent exemption is $300 per dependent.
//(d) Standard deduction and/or personal exemption is adjusted annually for inflation. Inflation-adjusted amounts for tax year 2025 are shown.(e) Arizona's standard deduction can be adjusted upward by an amount equal to 33 percent (as of TY 2024) of the amount the taxpayer would have claimed in charitable deductions if the taxpayer had claimed itemized deductions.
//(f) In lieu of a dependent exemption, Arizona offers a dependent tax credit of $100 per dependent under the age of 17 and $25 per dependent age 17 and older. The credit begins to phase out for taxpayers with federal adjusted gross income (FAGI) above $200,000 (single filers) or $400,000 (MFJ).
//(g) Rates apply to individuals earning more than $89,600. A separate tax tables exist for individuals earning $89,600 or less, with rates of 2 percent on income greater than or equal to $5,300; 3 percent on income greater than or equal to $10,600; 3.4 percent on income greater than or equal to $15,100; and 3.9 percent on income greater than $25,000 but less than or equal to $89,600.
//(h) Standard deduction or personal exemption is structured as a tax credit.
//(i) Connecticut and New York have "tax benefit recapture," by which many high-income taxpayers pay their top tax rate on all income, not just on amounts above the benefit threshold.
//(j) Bracket levels adjusted for inflation each year. Inflation-adjusted bracket widths for 2025 were not available as of publication, so table reflects 2024 inflation-adjusted bracket widths.
//(k) Exemption credits phase out for single taxpayers by $6 for each $2,500 of federal AGI above $237,035 and for MFJ filers by $12 for each $2,500 of federal AGI above $474,075. The credit cannot be reduced to below zero.
//(l) Rates include the additional mental health services tax at the rate of 1 percent on taxable income in excess of $1 million. Rates exclude a payroll tax of 1.1 percent to fund the state’s disability insurance program. Effecting as of TY 2024, there is no wage ceiling for this payroll tax, which means that the state’s top individual income tax rate on wage income becomes 14.4 percent.
//(m) State provides a state-defined personal exemption amount for each exemption available and/or deductible under the Internal Revenue Code. Under the Tax Cuts and Jobs Act, the personal exemption is set at $0 until 2026 but not eliminated. Because it is still available, these state-defined personal exemptions remain available in some states but are set to $0 in other states.
//(n) Standard deduction and/or personal exemption adjusted annually for inflation, but the 2025 inflation adjustment was not available at time of publication, so table reflects actual 2024 amount(s).
//(o) Colorado, Montana, New Mexico, North Dakota, and South Carolina include the federal standard deduction in their income starting point.
//(p) Connecticut has a complex set of phaseout provisions. For each single taxpayer whose Connecticut AGI exceeds $56,500, the amount of the taxpayer's Connecticut taxable income to which the 2 percent tax rate applies shall be reduced by $1,000 for each $5,000, or fraction thereof, by which the taxpayer's Connecticut AGI exceeds said amount. Any such amount will have a tax rate of 4.5 percent instead of 2 percent. Each single taxpayer whose Connecticut AGI exceeds $105,000 shall pay an amount equal to $25 for each $5,000, or fraction thereof, by which the taxpayer's Connecticut AGI exceeds $105,000, up to a maximum payment of $250. Additionally, each single taxpayer whose Connecticut AGI exceeds $200,000 shall pay an amount equal to $90 for each $5,000, or fraction thereof, by which the taxpayer's Connecticut AGI exceeds $200,000 but is less than $500,000, and by an additional $50 for each $5,000, or fraction thereof, by which the taxpayer’s AGI exceeds $500,000, up to a maximum payment of $3,150. For each MFJ taxpayer whose Connecticut AGI exceeds $100,500, the amount of the taxpayer's Connecticut taxable income to which the 2 percent tax rate applies shall be reduced by $2,000 for each $5,000, or fraction thereof, by which the taxpayer's Connecticut AGI exceeds said amount. Any such amount of Connecticut taxable income to which, as provided in the preceding sentence, the 2 percent tax rate does not apply shall be an amount to which the 4.5 percent tax rate shall apply. Each MFJ filer whose Connecticut AGI exceeds $210,000 shall pay an amount equal to $50 for each $10,000, or fraction thereof, by which the taxpayer's Connecticut AGI exceeds $210,000, up to a maximum payment of $500. Additionally, each MFJ taxpayer whose Connecticut AGI exceeds $400,000 shall pay, in addition to the amount above, an amount equal to $180 for each $10,000, or fraction thereof, by which the taxpayer's Connecticut AGI exceeds $400,000, up to a maximum of $5,400, and a further $100 for each $10,000, or fraction thereof, by which Connecticut AGI exceeds $1 million, up to a combined maximum payment of $6,300.
//(q) Connecticut taxpayers are also given personal tax credits (1-75%) based upon adjusted gross income.
//(r) Connecticut's personal exemption phases out by $1,000 for each $1,000, or fraction thereof, by which a single filer's Connecticut AGI exceeds $30,000 and a MFJ filer's Connecticut AGI exceeds $48,000.
//(s) In addition to the individual income tax rates, Delaware imposes a tax on lump-sum distributions.
//(t) Additionally, Hawaii allows any taxpayer, other than a corporation, acting as a business entity in more than one state and required by law to file a return, to report and pay a tax of 0.5 percent of its annual gross sales (1) where the taxpayer's only activities in Hawaii consist of sales, (2) when the taxpayer does not own or rent real estate or tangible personal property, and (3) when the taxpayer’s annual gross sales in or into Hawaii do not exceed $100,000. Haw. Rev. Stat. § 235-51 (2015).
//(u) Deduction and/or exemption tied to federal tax system. Federal deductions and exemptions are indexed for inflation, and where applicable, the tax year 2025 inflation-adjusted amounts are shown.
//(v) As of June 1, 2017, taxpayers cannot claim the personal exemption if their adjusted gross income exceeds $250,000 (single filers) or $500,000 (MFJ).
//(w) $1,000 is a base exemption. If dependents meet certain conditions, filers can take an additional $1,500 exemption for each. If a taxpayer is claiming a child as a dependent for the first taxable year in which the exemption is allowed, the taxpayer is now permitted to claim an amount of $3,000, instead of $1,500.
//(x) Standard deduction will be adjusted for inflation beginning in 2026.
//(y) Maine's personal exemption begins to phase out for taxpayers with income exceeding $323,900 (single filers) or $388,650 (MFJ). These phaseout thresholds are adjusted annually for inflation, but 2025 inflation adjustments were not available at the time of publication. The standard deduction amounts for 2025 are phased out for taxpayers with Maine income over $100,000 (single filers) or $200,050 (MFJ). The dependent personal exemption is structured as a tax credit and begins to phase out for taxpayers with income exceeding $200,000 (head of household) or $400,000 (married filing jointly).
//(z) The standard deduction is 15 percent of income with a minimum of $1,800 and a cap of $2,700 for single filers and married filing separately filers. The standard deduction is a minimum of $3,650 and capped at $5,450 for MFJ filers, head of household filers, and qualifying surviving spouses. The minimum and maximum standard deduction amounts are adjusted annually for inflation. 2025 inflation-adjusted amounts were not announced as of publication, so 2024 inflation-adjusted amounts are shown.
//(aa) The exemption amount has the following phaseout schedule: If AGI is above $100,000 for single filers and above $150,000 for married filers, the $3,200 exemption begins to be phased out. If AGI is above $150,000 for single filers and above $200,000 for married filers, the exemption is phased out entirely.
//(bb) Bracket levels adjusted for inflation each year. Inflation-adjusted bracket levels for 2025 are shown.(cc) For taxpayers whose AGI exceeds $119,475 (married filing separately) or $238,950 (all other filers), Minnesota’s standard deduction is reduced by 3 percent of the excess of the taxpayer’s federal AGI over the applicable amount but not over $165,150 (married filing separately) or $330,300 (all other filers), plus 10 percent of the taxpayer's federal adjusted gross income over the second threshold, or 80 percent of the standard deduction otherwise allowable, whichever is less.
//(dd) Ohio's personal exemption is $2,400 for an AGI of $40,000 or less, $2,150 if AGI is more than $40,000 but less than or equal to $80,000, and $1,900 if AGI is greater than $80,000.
//(ee) The personal exemption credit is not allowed if federal AGI exceeds $100,000 for single filers or $200,000 for MFJ.(ff) The phaseout range for the standard deduction, personal exemption, and dependency exemption is $254,250 to $283,250. For taxpayers with modified Federal AGI exceeding $283,250, no standard deduction, personal exemption, or dependency exemption is available.
//(gg) The standard deduction is taken in the form of a nonrefundable credit of 6 percent of the federal standard or itemized deduction amount, excluding the deduction for state or local income tax. This credit phases out at 1.3 cents per dollar of AGI above $17,652 ($35,304 for MFJ) as of 2024.
//(hh) For taxpayers with federal AGI that exceeds $150,000, the taxpayer will pay the greater of state income tax or 3 percent of federal AGI.
//(ii) The standard deduction begins to phase out at $19,549 in income for single filers and $28,209 in income for joint filers. The standard deduction phases out to zero at $132,549 for single filers and $155,169 for joint filers.
//(jj) In lieu of the suspended personal exemption, New Mexico offers a deduction of $4,000 for all but one of a taxpayer’s dependents.
//(kk) Taxpayers with net income greater than or equal to $89,600 but not greater than $92,700 shall reduce the amount of tax due by deducting a bracket adjustment amount.
//(ll) The dependent deduction/exemption is adjusted annually for inflation, but the 2025 amount was not available at the time of publication, so the 2024 amount is shown.
//(mm) Taxpayers also receive an additional deduction of $1,200 for each standard deduction box checked on federal Form 1040.(nn) California and Oregon do not fully index their top brackets.
//(oo) Minnesota has a surtax on individuals, estates, and trusts equal to 1% of net investment income over $1 million.
//(pp) Ohio's personal exemption is adjusted periodically based on changes in the GDP deflator.
//(qq) South Carolina's withholding tables for 2025 assume a top marginal rate of 6.2 percent.
//(rr) In Massachusetts, rates exclude the paid family and medical leave payroll tax. Employers must withhold 0.46 percent of employees' eligible wages. Additionally, employers with 25 or more employees contribute an extra 0.42 percent of eligible wages.
//(ss) In Washington, the rate excludes the 0.58 percent payroll tax that funds the long-term care insurance program (WA Cares). Employers withhold this tax from employees' gross wages.

sealed trait HasStateTaxDue:
  def stateTaxDue: TaxPayable
end HasStateTaxDue

sealed trait StateTaxRegime

object StateTaxRegime:
  import State.*

  def of(state: State): StateTaxRegime =
    state match
      case AK | FL | NV | NH | SD | TN | TX | WA | WY => NilStateTaxRegime

      case CO =>
        // From fed taxable income.
        FlatStateTaxRegime(
          StateTaxRate.unsafeFrom(4.4 / 100),
          ???,
          ???,
          ???,
          ???,
          ???
        )
      case IL =>
        FlatStateTaxRegime(
          StateTaxRate.unsafeFrom(4.95 / 100),
          ???,
          ???,
          ???,
          ???,
          ???
        )
      case IN =>
        FlatStateTaxRegime(
          StateTaxRate.unsafeFrom(3.05 / 100),
          ???,
          ???,
          ???,
          ???,
          ???
        )
      case MI =>
        FlatStateTaxRegime(
          StateTaxRate.unsafeFrom(4.25 / 100),
          ???,
          ???,
          ???,
          ???,
          ???
        )
      case NC =>
        FlatStateTaxRegime(
          StateTaxRate.unsafeFrom(4.25 / 100),
          ???,
          ???,
          ???,
          ???,
          ???
        )
      case PA =>
        FlatStateTaxRegime(
          StateTaxRate.unsafeFrom(3.07 / 100),
          ???,
          ???,
          ???,
          ???,
          ???
        )
      case UT =>
        FlatStateTaxRegime(
          StateTaxRate.unsafeFrom(4.85 / 100),
          ???,
          ???,
          ???,
          ???,
          ???
        )

      // Progressive: TODO
      case AL => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case AZ => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case AR => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case AS => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case CA => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case CT => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case DE => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case DC => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case GA => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case GU => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case HI => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case ID => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case IA => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case KS => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case KY => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case LA => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case ME => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case MD => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)

      case MA =>
        ProgressiveStateTaxRegime(
          rateFunctions = Map(
            Unmarried -> List(
              0         -> 5.0,
              1_083_159 -> 9.0
            ).asRateFunction,
            Married -> List(
              0         -> 5.0,
              1_083_159 -> 9.0
            ).asRateFunction
          ),
          personalExemptions = Map(
            Single          -> Deduction(4000),
            HeadOfHousehold -> Deduction(6800),
            Married         -> Deduction(8800)
          ),
          oldAgeExemption = (age: Int) => if age >= 65 then Deduction(700) else Deduction.zero,
          standardDeductions = _ => Deduction.zero,
          perDependentExemption = Deduction(1000),
          exemptionsAreCredits = false
        )
      case MN => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case MS => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case MO => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case MT => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case NE => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case NJ => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case NM => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case NY => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case ND => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case MP => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case OH => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case OK => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case OR => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case PR => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case RI => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case SC => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case TT => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case VT => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case VA => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case VI => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case WV => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
      case WI => ProgressiveStateTaxRegime(???, ???, ???, ???, ???, ???)
    end match
  end of

  extension (pairs: List[(threshold: Int, percentage: Double)])
    private def asRateFunction: RateFunction[StateTaxRate] =
      RateFunction.of(
        pairs.map: pair =>
          (IncomeThreshold(pair.threshold), StateTaxRate.unsafeFrom(pair.percentage / 100.0))
      )
  end extension
end StateTaxRegime

sealed trait HasStateTaxCalculator:
  // MA has these and they are not inflation-adjusted.
  def personalExemptions: FilingStatus => Deduction
  def perDependentExemption: Deduction
  def oldAgeExemption: Int => Deduction
  def exemptionsAreCredits: Boolean
  def standardDeductions: FilingStatus => Deduction

  def calculator(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): HasStateTaxDue

case object NilStateTaxRegime extends StateTaxRegime, HasStateTaxDue:
  override val stateTaxDue: TaxPayable = TaxPayable.zero
end NilStateTaxRegime

class FlatStateTaxRegime(
  @unused
  rate: StateTaxRate,
  override val standardDeductions: FilingStatus => Deduction,
  override val personalExemptions: FilingStatus => Deduction,
  override val oldAgeExemption: Int => Deduction,
  override val perDependentExemption: Deduction,
  override val exemptionsAreCredits: Boolean
) extends StateTaxRegime,
      HasStateTaxCalculator:
  override def calculator(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): HasStateTaxDue = ???

end FlatStateTaxRegime

class ProgressiveStateTaxRegime(
  @unused
  rateFunctions: MaritalStatus => RateFunction[StateTaxRate],
  override val personalExemptions: FilingStatus => Deduction,
  override val oldAgeExemption: Int => Deduction,
  // TODO: Should these be from FilingStatus?
  override val standardDeductions: FilingStatus => Deduction,
  override val perDependentExemption: Deduction,
  override val exemptionsAreCredits: Boolean
) extends StateTaxRegime,
      HasStateTaxCalculator:
  override def calculator(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): HasStateTaxDue = ???
end ProgressiveStateTaxRegime
