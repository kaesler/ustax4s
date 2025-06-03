package org.kae.ustax4s.gsheetfacade

import org.kae.ustax4s.federal.{BoundRegime, RMDs}
import scala.annotation.unused
import scala.scalajs.js.annotation.JSExportTopLevel

// To remove warning.
@unused
object Facade:
  import Conversions.given
  import TypeAliases.*

  /** Standard deduction for a known year. Example: TIR_STD_DEDUCTION(2022, "HeadOfHousehold",
    * 1955-10-02)
    *
    * @param {number}
    *   year the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {string}
    *   filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object}
    *   birthDate tax payer's date of birth
    * @returns
    *   {number} The standard deduction
    */
  @JSExportTopLevel("tir_std_deduction")
  @unused
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
    * @param {string}
    *   regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number}
    *   bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number}
    *   year a year in the future, after the current year
    * @param {string}
    *   filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object}
    *   birthDate tax payer's date of birth
    * @returns
    *   {number} The standard deduction
    */
  @JSExportTopLevel("tir_future_std_deduction")
  @unused
  def tir_future_std_deduction(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus,
    birthDate: GLocalDate
  ): GDeduction =
    BoundRegime
      .forFutureYear(
        regime,
        year,
        bracketInflationRate,
        filingStatus
      )
      .standardDeduction(birthDate)
  end tir_future_std_deduction

  /** Width of an ordinary income tax bracket for a known year. Example:
    * TIR_ORDINARY_BRACKET_WIDTH(2022, "Single", 10)
    *
    * @param {number}
    *   year a year between 2016 and the current year
    * @param {string}
    *   filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number}
    *   ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns
    *   {number} The width of the specified ordinary income tax bracket.
    */
  @JSExportTopLevel("tir_ordinary_bracket_width")
  @unused
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
    * @param {number}
    *   year a year between 2016 and the current year
    * @param {string}
    *   filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number}
    *   ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns
    *   {number} The end of the specified ordinary bracket
    */
  @JSExportTopLevel("tir_ordinary_bracket_end")
  @unused
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
    * @param {string}
    *   regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number}
    *   bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number}
    *   year a year in the future, after the current year
    * @param {string}
    *   filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number}
    *   ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns
    *   {number} The width of the specified ordinary income bracket.
    */
  @JSExportTopLevel("tir_future_ordinary_bracket_width")
  @unused
  def tir_future_ordinary_bracket_width(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus,
    bracketRatePercentage: GFederalTaxRate
  ): GTaxableIncome =
    BoundRegime
      .forFutureYear(
        regime,
        year,
        bracketInflationRate,
        filingStatus
      )
      .ordinaryBrackets
      .unsafeBracketWidth(bracketRatePercentage / 100)
  end tir_future_ordinary_bracket_width

  /** End of an ordinary income tax bracket for a future year. Example:
    * TIR_FUTURE_ORDINARY_BRACKET_END("PreTCJA", 2030, "HeadOfHousehold", 10)
    *
    * @param {string}
    *   regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number}
    *   bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number}
    *   year a year in the future, after the current year
    * @param {string}
    *   filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number}
    *   ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns
    *   {number} The end of the specified ordinary income bracket.
    */
  @JSExportTopLevel("tir_future_ordinary_bracket_end")
  @unused
  def tir_future_ordinary_bracket_end(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus,
    bracketRatePercentage: GFederalTaxRate
  ): GTaxableIncome =
    BoundRegime
      .forFutureYear(
        regime,
        year,
        bracketInflationRate,
        filingStatus
      )
      .ordinaryBrackets
      .unsafeTaxableIncomeToEndOfBracket(bracketRatePercentage / 100)
  end tir_future_ordinary_bracket_end

  /** Threshold above which long term capital gains are taxed, for a known year. Example:
    * TIR_LTCG_TAX_START(2022, "HeadOfHousehold")
    *
    * @param {number}
    *   year a year between 2016 and the current year
    * @param {string}
    *   filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @returns
    *   {number} the end of the zero tax rate on qualified investment income
    */
  @JSExportTopLevel("tir_ltcg_tax_start")
  @unused
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
    * @param {string}
    *   regime the tax regime to use, one of "TCJA", "PreTCJA"
    * @param {number}
    *   year a year in the future, after the current year
    * @param {number}
    *   bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {string}
    *   filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @returns
    *   {number} the end of the zero tax rate on qualified investment income
    */
  @JSExportTopLevel("tir_future_ltcg_tax_start")
  @unused
  def tir_future_ltcg_tax_start(
    regime: GRegime,
    bracketInflationRate: Double,
    year: GYear,
    filingStatus: GFilingStatus
  ): GIncomeThreshold =
    BoundRegime
      .forFutureYear(regime, year, bracketInflationRate, filingStatus)
      .qualifiedBrackets
      .startOfNonZeroQualifiedRateBracket
  end tir_future_ltcg_tax_start

  /** The RMD fraction for a given age. Example: TIR_RMD_FRACTION_FOR_AGE(76)
    *
    * @param {number}
    *   age age of the taxpayer
    * @returns
    *   {number} the RMD fraction
    */
  @JSExportTopLevel("tir_rmd_fraction_for_age")
  @unused
  def tir_rmd_fraction_for_age(
    age: Int
  ): Double =
    RMDs.fractionForAge(age)

//  /**
//   * The Federal tax due for a known year.
//   * Example: TIR_FEDERAL_TAX_DUE(2022, "Single", 1955-10-02, 0, 10000, 40000, 5000, 0)
//   *
//   * @param {number} year a year between 2016 and the current year
//   * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
//   * @param {object} birthDate tax payer's date of birth
//   * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
//   * @param {number} socSec Social Security benefits received
//   * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
//   * @param {number} qualifiedIncome qualified dividends and long term capital gains
//   * @param {number} itemizedDeductions total of any itemized deductions
//   * @returns {number} the Federal tax due
//   * @customfunction
//   */
//  function TIR_FEDERAL_TAX_DUE (
//    year,
//    filingStatus,
//    birthDate,
//    personalExemptions,
//    socSec,
//    ordinaryIncomeNonSS,
//    qualifiedIncome,
//    itemizedDeductions
//  ) {
//    const psYear = unsafeMakeYear(year);
//    const psFilingStatus = unsafeReadFilingStatus(filingStatus);
//    const psBirthDate = toPurescriptDate(birthDate);
//
//    return taxDueForKnownYear(
//      psYear)(
//      psFilingStatus)(
//      psBirthDate)(
//      personalExemptions)(
//      socSec)(
//      ordinaryIncomeNonSS)(
//      qualifiedIncome)(
//      itemizedDeductions);
//  }
//
//  function TIR_FEDERAL_TAX_RESULTS (
//    year,
//    filingStatus,
//    birthDate,
//    personalExemptions,
//    socSec,
//    ordinaryIncomeNonSS,
//    qualifiedIncome,
//    itemizedDeductions
//  ) {
//    const psYear = unsafeMakeYear(year);
//    const psFilingStatus = unsafeReadFilingStatus(filingStatus);
//    const psBirthDate = toPurescriptDate(birthDate);
//
//    const res = taxResultsForKnownYearAsTable(
//      psYear)(
//      psFilingStatus)(
//      psBirthDate)(
//      personalExemptions)(
//      socSec)(
//      ordinaryIncomeNonSS)(
//      qualifiedIncome)(
//      itemizedDeductions);
//
//    return res;
//  }
//
//
//  /**
//   * The Federal tax due for a future year.
//   * Example: TIR_FUTURE_FEDERAL_TAX_DUE("TCJA", 2023, 0.034, "Single", 1955-10-02, 0, 10000, 40000, 5000, 0)
//   *
//   * @param {string} regime the tax regime to use, one of "TCJA", "PreTCJA"
//   * @param {number} year a year in the future, after the current year
//   * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
//   * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
//   * @param {object} birthDate tax payer's date of birth
//   * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
//   * @param {number} socSec Social Security benefits received
//   * @param {number} ordinaryIncomeNonSS  ordinary income excluding Social Security
//   * @param {number} qualifiedIncome qualified dividends and long term capital gains
//   * @param {number} itemizedDeductions total of any itemized deductions
//   * @returns {number} the Federal tax due
//   * @customfunction
//   */
//  function TIR_FUTURE_FEDERAL_TAX_DUE (
//    regime,
//    year,
//    bracketInflationRate,
//    filingStatus,
//    birthDate,
//    personalExemptions,
//    socSec,
//    ordinaryIncomeNonSS,
//    qualifiedIncome,
//    itemizedDeductions
//  ) {
//    const psRegime = unsafeReadRegime(regime);
//    const psYear = unsafeMakeYear(year);
//    const inflationFactorEstimate = 1.0 + bracketInflationRate;
//    const psFilingStatus = unsafeReadFilingStatus(filingStatus);
//    const psBirthDate = toPurescriptDate(birthDate);
//
//    return taxDueForFutureYear(
//      psRegime)(
//      psYear)(
//      inflationFactorEstimate)(
//      psFilingStatus)(
//      psBirthDate)(
//      personalExemptions)(
//      socSec)(
//      ordinaryIncomeNonSS)(
//      qualifiedIncome)(
//      itemizedDeductions);
//  }
//
//
//  /**
//   * The marginal tax rate on ordinary income for a known year.
//   * Example: TIR_FEDERAL_TAX_SLOPE(2022, "Single", 1955-10-02, 0, 10000, 40000, 5000, 0, 1000)
//   *
//   * @param {number} year a year between 2016 and the current year
//   * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
//   * @param {object} birthDate tax payer's date of birth
//   * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
//   * @param {number} socSec Social Security benefits received
//   * @param {number} ordinaryIncomeNonSS  ordinary income excluding Social Security
//   * @param {number} qualifiedIncome qualified dividends and long term capital gains
//   * @param {number} itemizedDeductions total of any itemized deductions
//   * @param {number} ordinaryIncomeDelta the change to ordinaryIncomeNonSS with which to calculate a slope
//   * @returns {number} the marginal tax rate as a percentage.
//   * @customfunction
//   */
//  function TIR_FEDERAL_TAX_SLOPE (
//    year,
//    filingStatus,
//    birthDate,
//    personalExemptions,
//    socSec,
//    ordinaryIncomeNonSS,
//    qualifiedIncome,
//    itemizedDeductions,
//    ordinaryIncomeDelta
//  ) {
//    const start = Math.min(
//      ordinaryIncomeNonSS,
//      ordinaryIncomeNonSS + ordinaryIncomeDelta)
//    const end = Math.max(
//      ordinaryIncomeNonSS,
//      ordinaryIncomeNonSS + ordinaryIncomeDelta)
//
//    const federalTaxAtStart = TIR_FEDERAL_TAX_DUE(
//      year,
//      filingStatus,
//      birthDate,
//      personalExemptions,
//      socSec,
//      start,
//      qualifiedIncome,
//      itemizedDeductions
//    );
//    const federalTaxAtEnd = TIR_FEDERAL_TAX_DUE(
//      year,
//      filingStatus,
//      birthDate,
//      personalExemptions,
//      socSec,
//      end,
//      qualifiedIncome,
//      itemizedDeductions
//    );
//    const deltaY = (federalTaxAtEnd - federalTaxAtStart);
//
//    return deltaY / Math.abs(ordinaryIncomeDelta);
//  }
//
//  /**
//   * The marginal tax rate on ordinary income for a future year.
//   * Example: TIR_FUTURE_FEDERAL_TAX_SLOPE("TCJA", 2023, 0.034, "Single", 1955-10-02, 0, 10000, 40000, 5000, 0, 1000)
//   *
//   * @param {string} regime the tax regime to use, one of "TCJA", "PreTCJA"
//   * @param {number} year a year in the future, after the current year
//   * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
//   * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
//   * @param {object} birthDate tax payer's date of birth
//   * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
//   * @param {number} socSec Social Security benefits received
//   * @param {number} ordinaryIncomeNonSS  ordinary income excluding Social Security
//   * @param {number} qualifiedIncome qualified dividends and long term capital gains
//   * @param {number} itemizedDeductions total of any itemized deductions
//   * @param {number} ordinaryIncomeDelta the change to ordinaryIncomeNonSS with which to calculate a slope
//   * @returns {number} the marginal tax rate as a percentage.
//   * @customfunction
//   */
//  function TIR_FUTURE_FEDERAL_TAX_SLOPE (
//    regime,
//    year,
//    bracketInflationRate,
//    filingStatus,
//    birthDate,
//    personalExemptions,
//    socSec,
//    ordinaryIncomeNonSS,
//    qualifiedIncome,
//    itemizedDeductions,
//    ordinaryIncomeDelta
//  ) {
//    const start = Math.min(
//      ordinaryIncomeNonSS,
//      ordinaryIncomeNonSS + ordinaryIncomeDelta)
//    const end = Math.max(
//      ordinaryIncomeNonSS,
//      ordinaryIncomeNonSS + ordinaryIncomeDelta)
//
//    const federalTaxAtStart = TIR_FUTURE_FEDERAL_TAX_DUE(
//      regime,
//      year,
//      bracketInflationRate,
//      filingStatus,
//      birthDate,
//      personalExemptions,
//      socSec,
//      start,
//      qualifiedIncome,
//      itemizedDeductions
//    );
//    const federalTaxAtEnd = TIR_FUTURE_FEDERAL_TAX_DUE(
//      regime,
//      year,
//      bracketInflationRate,
//      filingStatus,
//      birthDate,
//      personalExemptions,
//      socSec,
//      end,
//      qualifiedIncome,
//      itemizedDeductions
//    );
//    const deltaY = (federalTaxAtEnd - federalTaxAtStart);
//
//    return deltaY / Math.abs(ordinaryIncomeDelta);
//  }
//
//  /**
//   * The amount of Social Security income that is taxable.
//   * Example: TIR_TAXABLE_SOCIAL_SECURITY("HeadOfHousehold", 20000, 52000)
//   *
//   * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
//   * @param {number} ssRelevantOtherIncome all income apart from Social Security
//   * @param {number} socSec Social Security benefits received
//   * @returns {number} the amount of Social Security income that is taxable
//   * @customfunction
//   */
//  function TIR_TAXABLE_SOCIAL_SECURITY
//    (filingStatus, ssRelevantOtherIncome, socSec) {
//      const psFilingStatus = unsafeReadFilingStatus(filingStatus);
//
//      return amountTaxable(psFilingStatus)(socSec)(ssRelevantOtherIncome);
//    }
//
//  /** *
//   * The MA state income tax due.
//   * Example: TIR_MA_STATE_TAX_DUE(2022, "Married", 1955-10-02, 0, 130000)
//   *
//   * @param {number} year a year between 2016 and the current year
//   * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
//   * @param {object} birthDate tax payer's date of birth
//   * @param {number} dependents
//   * @param {number} massachusettsGrossIncome
//   * @returns {number} the MA state income tax due.
//   * @customfunction
//   */
//  function TIR_MA_STATE_TAX_DUE (
//    year,
//    filingStatus,
//    birthDate,
//    dependents,
//    massachusettsGrossIncome
//  ) {
//    const psYear = unsafeMakeYear(year);
//    const psFilingStatus = unsafeReadFilingStatus(filingStatus);
//    const psBirthDate = toPurescriptDate(birthDate);
//
//    return maStateTaxDue(
//      psYear)(
//      psFilingStatus)(
//      psBirthDate)(
//      dependents)(
//      massachusettsGrossIncome);
//  }
//
//  function bindRegimeForKnownYear (yearAsNumber, filingStatusName) {
//    const psFilingStatus = unsafeReadFilingStatus(filingStatusName);
//    const psYear = unsafeMakeYear(yearAsNumber);
//
//    return boundRegimeForKnownYear(psYear)(psFilingStatus);
//  }
//
//  function bindRegimeForFutureYear
//    (regimeName, yearAsNumber, bracketInflationRate, filingStatusName) {
//      const psRegime = unsafeReadRegime(regimeName);
//      const psYear = unsafeMakeYear(yearAsNumber);
//      const inflationFactorEstimate = 1.0 + bracketInflationRate;
//      const psFilingStatus = unsafeReadFilingStatus(filingStatusName);
//
//      return boundRegimeForFutureYear(psRegime)(psYear)(inflationFactorEstimate)(
//        psFilingStatus);
//    }
//
//  function toPurescriptDate (dateObject) {
//    if (typeof(dateObject) != "object")
//      throw "Date object required";
//    if (!dateObject instanceof Date)
//      throw "Date object required";
//    const year = 1900 + dateObject.getYear();
//    const month = 1 + dateObject.getMonth();
//    const dayOfMonth = dateObject.getDate();
//    return unsafeMakeDate(year)(month)(dayOfMonth);
//  }
//
//  /**
//   * Runs when the add-on is installed.
//   */
//  function onInstall () {
//    onOpen();
//  }
//
//  /**
//   * Runs when the document is opened, creating the add-on's menu. Custom function
//   * add-ons need at least one menu item, since the add-on is only enabled in the
//   * current spreadsheet when a function is run.
//   */
//  function onOpen () {
//    SpreadsheetApp.getUi().createAddonMenu()
//      .addItem('Use in this spreadsheet
//    ', 'use
//    ')
//  .addToUi();
//  }
//
//  /**
//   * Enables the add-on on for the current spreadsheet (simply by running) and
//   * shows a popup informing the user of the new functions that are available.
//   */
//  function use () {
//    var title = 'Tax In Retirement Functions
//    ';
//    var message = 'The Tax In Retirement functions are now available in
//    '+'this spreadsheet
//  .More information is available in the function help
//    '+'box that appears when you start using them in a formula.
//    ';
//    var ui = SpreadsheetApp.getUi();
//    ui.alert(title, message, ui.ButtonSet.OK);
//  }

end Facade
