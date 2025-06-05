package org.kae.ustax4s.gsheetfacade

import org.kae.ustax4s.federal.{BoundRegime, RMDs, TaxableSocialSecurity}
import org.kae.ustax4s.state_ma.StateMATaxCalculator
import scala.scalajs.js.annotation.JSExportTopLevel

object Facade:
  import Conversions.given
  import TypeAliases.*

  /** Standard deduction for a known year. Example: TIR_STD_DEDUCTION(2022, "HeadOfHousehold",
    * 1955-10-02)
    *
    * @param {number} year the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @returns  {number} The standard deduction
    */
  @JSExportTopLevel("tir_std_deduction")
  def tir_std_deduction(
    year: GYear,
    filingStatus: GFilingStatus,
    birthDate: GLocalDate
  ): GDeduction =
    BoundRegime
      .forKnownYear(year, filingStatus)
      .standardDeduction(birthDate)
  end tir_std_deduction

  /** Standard deduction for a future year. Example: TIR_FUTURE_STD_DEDUCTION("TCJA", 3%, 2030,
    * "HeadOfHousehold", 1955-10-02)
    *
    * @param {string} regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year a year in the future, after the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @returns {number} The standard deduction
    */
  @JSExportTopLevel("tir_future_std_deduction")
  def tir_future_std_deduction(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus,
    birthDate: GLocalDate
  ): GDeduction =
    val bracketInflationFactor = 1.0 + bracketInflationRate
    BoundRegime
      .forFutureYear(
        regime,
        year,
        bracketInflationFactor,
        filingStatus
      )
      .standardDeduction(birthDate)
  end tir_future_std_deduction

  /** Width of an ordinary income tax bracket for a known year. Example:
    * TIR_ORDINARY_BRACKET_WIDTH(2022, "Single", 10)
    *
    * @param {number} year a year between 2016 and the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number} ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns {number} The width of the specified ordinary income tax bracket.
    */
  @JSExportTopLevel("tir_ordinary_bracket_width")
  def tir_ordinary_bracket_width(
    year: GYear,
    filingStatus: GFilingStatus,
    bracketRatePercentage: GFederalTaxRate
  ): GTaxableIncome =
    BoundRegime
      .forKnownYear(year, filingStatus)
      .ordinaryBrackets
      .unsafeBracketWidth(bracketRatePercentage / 100)
  end tir_ordinary_bracket_width

  /** End of an ordinary income tax bracket for a known year. Example:
    * TIR_ORDINARY_BRACKET_END(2022, "Single", 10)
    *
    * @param {number} year a year between 2016 and the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number} ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns {number} The end of the specified ordinary bracket
    */
  @JSExportTopLevel("tir_ordinary_bracket_end")
  def tir_ordinary_bracket_end(
    year: GYear,
    filingStatus: GFilingStatus,
    bracketRatePercentage: GFederalTaxRate
  ): GTaxableIncome =
    BoundRegime
      .forKnownYear(year, filingStatus)
      .ordinaryBrackets
      .unsafeTaxableIncomeToEndOfBracket(bracketRatePercentage / 100)
  end tir_ordinary_bracket_end

  /** Width of an ordinary income tax bracket for a future year. Example:
    * TIR_FUTURE_ORDINARY_BRACKET_WIDTH("PreTCJA", 2030, "HeadOfHousehold", 10)
    *
    * @param {string} regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year a year in the future, after the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number} ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns {number} The width of the specified ordinary income bracket.
    */
  @JSExportTopLevel("tir_future_ordinary_bracket_width")
  def tir_future_ordinary_bracket_width(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus,
    bracketRatePercentage: GFederalTaxRate
  ): GTaxableIncome =
    val bracketInflationFactor = 1.0 + bracketInflationRate
    BoundRegime
      .forFutureYear(
        regime,
        year,
        bracketInflationFactor,
        filingStatus
      )
      .ordinaryBrackets
      .unsafeBracketWidth(bracketRatePercentage / 100)
  end tir_future_ordinary_bracket_width

  /** End of an ordinary income tax bracket for a future year. Example:
    * TIR_FUTURE_ORDINARY_BRACKET_END("PreTCJA", 2030, "HeadOfHousehold", 10)
    *
    * @param {string} regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year a year in the future, after the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number} ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns {number} The end of the specified ordinary income bracket.
    */
  @JSExportTopLevel("tir_future_ordinary_bracket_end")
  def tir_future_ordinary_bracket_end(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus,
    bracketRatePercentage: GFederalTaxRate
  ): GTaxableIncome =
    val bracketInflationFactor = 1.0 + bracketInflationRate
    BoundRegime
      .forFutureYear(
        regime,
        year,
        bracketInflationFactor,
        filingStatus
      )
      .ordinaryBrackets
      .unsafeTaxableIncomeToEndOfBracket(bracketRatePercentage / 100)
  end tir_future_ordinary_bracket_end

  /** Threshold above which long term capital gains are taxed, for a known year. Example:
    * TIR_LTCG_TAX_START(2022, "HeadOfHousehold")
    *
    * @param {number} year a year between 2016 and the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @returns {number} the end of the zero tax rate on qualified investment income
    */
  @JSExportTopLevel("tir_ltcg_tax_start")
  def tir_ltcg_tax_start(
    year: GYear,
    filingStatus: GFilingStatus
  ): GIncomeThreshold =
    BoundRegime
      .forKnownYear(year, filingStatus)
      .qualifiedBrackets
      .startOfNonZeroQualifiedRateBracket
  end tir_ltcg_tax_start

  /** Threshold above which long term capital gains are taxed, for a future year Example:
    * TIR_FUTURE_LTCG_TAX_START("PreTCJA", 2027, 3.4%, "HeadOfHousehold")
    *
    * @param {string} regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number} year a year in the future, after the current year
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @returns {number} the end of the zero tax rate on qualified investment income
    */
  @JSExportTopLevel("tir_future_ltcg_tax_start")
  def tir_future_ltcg_tax_start(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus
  ): GIncomeThreshold =
    val bracketInflationFactor = 1.0 + bracketInflationRate
    BoundRegime
      .forFutureYear(regime, year, bracketInflationFactor, filingStatus)
      .qualifiedBrackets
      .startOfNonZeroQualifiedRateBracket
  end tir_future_ltcg_tax_start

  /** The RMD fraction for a given age. Example: TIR_RMD_FRACTION_FOR_AGE(76)
    *
    * @param {number} age age of the taxpayer
    * @returns {number} the RMD fraction
    */
  @JSExportTopLevel("tir_rmd_fraction_for_age")
  def tir_rmd_fraction_for_age(
    age: Int
  ): Double =
    RMDs.fractionForAge(age)

  /** The Federal tax due for a known year. Example: TIR_FEDERAL_TAX_DUE(2022, "Single", 1955-10-02,
    * 0, 10000, 40000, 5000, 0)
    *
    * @param {number} year a year between 2016 and the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {number} the Federal tax due
    */
  @JSExportTopLevel("tir_federal_tax_due")
  def tir_federal_tax_due(
    year: GYear,
    filingStatus: GFilingStatus,
    birthDate: GLocalDate,
    personalExemptions: Int,
    socSec: GIncome,
    ordinaryIncomeNonSS: GIncome,
    qualifiedIncome: GTaxableIncome,
    itemizedDeductions: GDeduction
  ): GTaxPayable =
    BoundRegime
      .forKnownYear(year, filingStatus)
      .calculator
      .federalTaxResults(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .taxDue
  end tir_federal_tax_due

  /** The Federal tax due for a future year. Example: TIR_FUTURE_FEDERAL_TAX_DUE("TCJA", 2023,
    * 0.034, "Single", 1955-10-02, 0, 10000, 40000, 5000, 0)
    *
    * @param {string} regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number} year a year in the future, after the current year
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {number} the Federal tax due
    */
  @JSExportTopLevel("tir_future_federal_tax_due")
  def tir_future_federal_tax_due(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus,
    birthDate: GLocalDate,
    personalExemptions: Int,
    socSec: GIncome,
    ordinaryIncomeNonSS: GIncome,
    qualifiedIncome: GTaxableIncome,
    itemizedDeductions: GDeduction
  ): GTaxPayable = {
    val bracketInflationFactor = 1.0 + bracketInflationRate
    BoundRegime
      .forFutureYear(regime, year, bracketInflationFactor, filingStatus)
      .calculator
      .federalTaxResults(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .taxDue
  }
  end tir_future_federal_tax_due

  /** The marginal tax rate on ordinary income for a known year. Example:
    * TIR_FEDERAL_TAX_SLOPE(2022, "Single", 1955-10-02, 0, 10000, 40000, 5000, 0, 1000)
    *
    * @param {number} year a year between 2016 and the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @param {number} ordinaryIncomeDelta the change to ordinaryIncomeNonSS with which to calculate a slope
    * @returns {number} the marginal tax rate as a percentage.
    */
  @JSExportTopLevel("tir_federal_tax_slope")
  def tir_federal_tax_slope(
    year: GYear,
    filingStatus: GFilingStatus,
    birthDate: GLocalDate,
    personalExemptions: Int,
    socSec: GIncome,
    ordinaryIncomeNonSS: GIncome,
    qualifiedIncome: GTaxableIncome,
    itemizedDeductions: GDeduction,
    ordinaryIncomeNonSSDelta: GIncome
  ): Double =
    val start = math.min(ordinaryIncomeNonSS, ordinaryIncomeNonSS + ordinaryIncomeNonSSDelta)
    val end   = math.max(ordinaryIncomeNonSS, ordinaryIncomeNonSS + ordinaryIncomeNonSSDelta)
    val taxDueAtStart = tir_federal_tax_due(
      year,
      filingStatus,
      birthDate,
      personalExemptions,
      socSec,
      start,
      qualifiedIncome,
      itemizedDeductions
    )
    val taxDueAtEnd = tir_federal_tax_due(
      year,
      filingStatus,
      birthDate,
      personalExemptions,
      socSec,
      end,
      qualifiedIncome,
      itemizedDeductions
    )
    (taxDueAtEnd - taxDueAtStart) / math.abs(ordinaryIncomeNonSSDelta)
  end tir_federal_tax_slope

  /** The marginal tax rate on ordinary income for a future year. Example:
    * TIR_FUTURE_FEDERAL_TAX_SLOPE("TCJA", 2023, 0.034, "Single", 1955-10-02, 0, 10000, 40000, 5000,
    * 0, 1000)
    *
    * @param {string} regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number} year a year in the future, after the current year
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @param {number} ordinaryIncomeDelta the change to ordinaryIncomeNonSS with which to calculate a slope
    * @returns {number} the marginal tax rate as a percentage.
    */
  @JSExportTopLevel("tir_future_federal_tax_slope")
  def tir_future_federal_tax_slope(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus,
    birthDate: GLocalDate,
    personalExemptions: Int,
    socSec: GIncome,
    ordinaryIncomeNonSS: GIncome,
    qualifiedIncome: GTaxableIncome,
    itemizedDeductions: GDeduction,
    ordinaryIncomeNonSSDelta: GIncome
  ): Double =
    val start = math.min(ordinaryIncomeNonSS, ordinaryIncomeNonSS + ordinaryIncomeNonSSDelta)
    val end   = math.max(ordinaryIncomeNonSS, ordinaryIncomeNonSS + ordinaryIncomeNonSSDelta)
    val taxDueAtStart = tir_future_federal_tax_due(
      regime,
      bracketInflationRate,
      year,
      filingStatus,
      birthDate,
      personalExemptions,
      socSec,
      start,
      qualifiedIncome,
      itemizedDeductions
    )
    val taxDueAtEnd = tir_future_federal_tax_due(
      regime,
      bracketInflationRate,
      year,
      filingStatus,
      birthDate,
      personalExemptions,
      socSec,
      end,
      qualifiedIncome,
      itemizedDeductions
    )
    (taxDueAtEnd - taxDueAtStart) / math.abs(ordinaryIncomeNonSSDelta)
  end tir_future_federal_tax_slope

  /** The amount of Social Security income that is taxable.
    * Example: TIR_TAXABLE_SOCIAL_SECURITY("HeadOfHousehold", 20000, 52000)
    *
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number} ssRelevantOtherIncome all income apart from Social Security
    * @param {number} socSec Social Security benefits received
    * @returns {number} the amount of Social Security income that is taxable
    */
  @JSExportTopLevel("tir_taxable_social_security")
  def tir_taxable_social_security(
    filingStatus: GFilingStatus,
    ssRelevantOtherIncome: GIncome,
    socSec: GIncome
  ): GIncome =
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      filingStatus,
      socSec,
      ssRelevantOtherIncome
    )
  end tir_taxable_social_security

  /** The MA state income tax due.
    * Example: TIR_MA_STATE_TAX_DUE(2022, "Married", 1955-10-02, 0, 130000)
    *
    * @param {number} year a year between 2016 and the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} dependents
    * @param {number} massachusettsGrossIncome
    * @returns {number} the MA state income tax due.
    */
  @JSExportTopLevel("tir_ma_state_tax_due")
  def tir_ma_state_tax_due(
    year: GYear,
    filingStatus: GFilingStatus,
    birthDate: GLocalDate,
    dependents: Int,
    massachusettsGrossIncome: GIncome
  ): GTaxPayable =
    StateMATaxCalculator.taxDue(year, filingStatus, birthDate, dependents)(
      massachusettsGrossIncome
    )
  end tir_ma_state_tax_due

end Facade
