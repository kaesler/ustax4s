package org.kae.ustax4s.gsheetfacade

import gsheets.cells.Cell
import gsheets.customfunctions.Input
import org.kae.ustax4s.federal.{BoundRegime, RMDs, TaxableSocialSecurity}
import org.kae.ustax4s.gsheetfacade.Conversions.Output
import org.kae.ustax4s.money.Moneys.{Deduction, Income, TaxPayable}
import org.kae.ustax4s.state_ma.StateMATaxCalculator
import scala.scalajs.js.annotation.JSExportTopLevel

object Facade:
  import Conversions.given

  /** The RMD fraction for a given age. Example: TIR_RMD_FRACTION_FOR_AGE(76)
    *
    * @param {number} age age of the taxpayer
    * @returns {number} the RMD fraction
    */
  @JSExportTopLevel("tf_rmd_fraction_for_age")
  def tf_rmd_fraction_for_age(age: Input): Output =
    RMDs.fractionForAge(age)
  end tf_rmd_fraction_for_age

  /** The amount of Social Security income that is taxable.
    * Example: TF_TAXABLE_SOCIAL_SECURITY("HeadOfHousehold", 20000, 52000)
    *
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number} ssRelevantOtherIncome all income apart from Social Security
    * @param {number} socSec Social Security benefits received
    * @returns {number} the amount of Social Security income that is taxable
    */
  @JSExportTopLevel("tf_taxable_social_security")
  def tf_taxable_social_security(
    filingStatus: Input,
    ssRelevantOtherIncome: Input,
    socSec: Input
  ): Output =
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      filingStatus,
      socSec,
      ssRelevantOtherIncome
    )
  end tf_taxable_social_security

  /** End of an ordinary income tax bracket. Example:
    * TF_ORDINARY_BRACKET_END(3%, 2030, "HeadOfHousehold", 10)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year the tax year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number} ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns {number} The end of the specified ordinary income bracket.
    */
  @JSExportTopLevel("tf_ordinary_bracket_end")
  def tf_ordinary_bracket_end(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    bracketRatePercentage: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .ordinaryRateFunction
      .unsafeTaxableIncomeToEndOfBracket(
        (bracketRatePercentage: Double) / 100
      )
  end tf_ordinary_bracket_end

  /** Width of an ordinary income tax bracket. Example:
    * TF_ORDINARY_BRACKET_WIDTH(3%, 2030, "HeadOfHousehold", 10)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year tax year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {number} ordinaryRatePercentage rate for a tax bracket e.g. 22
    * @returns {number} The width of the specified ordinary income bracket.
    */
  @JSExportTopLevel("tf_ordinary_bracket_width")
  def tf_ordinary_bracket_width(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    bracketRatePercentage: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(
        year,
        bracketInflationFactor,
        filingStatus
      )
      .ordinaryRateFunction
      .unsafeBracketWidth((bracketRatePercentage: Double) / 100)
  end tf_ordinary_bracket_width

  /** Threshold above which long term capital gains are taxed. Example:
    * TF_LTCG_TAX_START(3%, 2027, "HeadOfHousehold")
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year the tax year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @returns {number} the end of the zero tax rate on qualified investment income
    */
  @JSExportTopLevel("tf_ltcg_tax_start")
  def tf_ltcg_tax_start(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .qualifiedRateFunction
      .startOfNonZeroQualifiedRateBracket
  end tf_ltcg_tax_start

  /** Standard deduction. Example: TF_STD_DEDUCTION(3%, 2030,
    * "HeadOfHousehold", 1955-10-02)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year the tax year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @returns {number} The standard deduction
    */
  @JSExportTopLevel("tf_std_deduction")
  def tf_std_deduction(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(
        year,
        bracketInflationFactor,
        filingStatus
      )
      .standardDeduction(birthDate)
  end tf_std_deduction

  /** The Federal AGI. Example: TF_AGI(0.34, 2023,
    * "Single", 1955-10-02, 0, 10000, 40000, 5000, 0)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year the tax year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {number} the Federal AGI
    */
  @JSExportTopLevel("tf_agi")
  def tf_agi(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    personalExemptions: Input,
    socSec: Input,
    ordinaryIncomeNonSS: Input,
    qualifiedIncome: Input,
    itemizedDeductions: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .calculator
      .results(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .agi
  end tf_agi

  /** The Federal means tested senior deduction. Example: TF_SENIOR_DEDUCTION(0.34, 2023,
    * "Single", 1955-10-02, 0, 10000, 40000, 5000, 0)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year the tax year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {number} the means tested senior deduction
    */
  @JSExportTopLevel("tf_senior_deduction")
  def tf_senior_deduction(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    personalExemptions: Input,
    socSec: Input,
    ordinaryIncomeNonSS: Input,
    qualifiedIncome: Input,
    itemizedDeductions: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .calculator
      .results(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .meansTestedSeniorDeduction
  end tf_senior_deduction

  /** The Federal net deduction. Example: TF_NET_DEDUCTION(0.34, 2023,
    * "Single", 1955-10-02, 0, 10000, 40000, 5000, 0)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year tax year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {number} the net deduction
    */
  @JSExportTopLevel("tf_net_deduction")
  def tf_net_deduction(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    personalExemptions: Input,
    socSec: Input,
    ordinaryIncomeNonSS: Input,
    qualifiedIncome: Input,
    itemizedDeductions: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .calculator
      .results(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .netDeduction
  end tf_net_deduction

  /** The Federal tax due. Example: TF_TAX_DUE(3%, 2023,
    * "Single", 1955-10-02, 0, 10000, 40000, 5000, 0)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year the tax year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {number} the Federal tax due
    */
  @JSExportTopLevel("tf_tax_due")
  def tf_tax_due(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    personalExemptions: Input,
    socSec: Input,
    ordinaryIncomeNonSS: Input,
    qualifiedIncome: Input,
    itemizedDeductions: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .calculator
      .results(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .taxDue
  end tf_tax_due

  /** The Federal tax results. Example: TF_TAX_RESULTS(3%, 2023,
    * "Single", 1955-10-02, 0, 10000, 40000, 5000, 0)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year the tax year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {grid} the Federal tax results
    */
  @JSExportTopLevel("tf_tax_results")
  def tf_tax_results(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    personalExemptions: Input,
    socSec: Input,
    ordinaryIncomeNonSS: Input,
    qualifiedIncome: Input,
    itemizedDeductions: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    val r                      = BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .calculator
      .results(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )

    given Conversion[String, Cell]     = s => Cell(s)
    given Conversion[Deduction, Cell]  = d => Cell(d.asDouble)
    given Conversion[Double, Cell]     = Cell.apply
    given Conversion[Income, Cell]     = i => Cell(i.asDouble)
    given Conversion[TaxPayable, Cell] = t => Cell(t.asDouble)
    type ConversionToCell[T] = Conversion[T, Cell]

    def cp[T: ConversionToCell](s: String, t: T): Vector[Cell] =
      Vector(s, t: Cell)

    Vector(
      cp("taxableSocialSecurity", r.taxableSocialSecurity),
      cp("agi", r.agi),
      cp("personalExemptionDeduction", r.personalExemptionDeduction),
      cp("standardDeduction", r.standardDeduction),
      cp("meansTestedSeniorDeduction", r.meansTestedSeniorDeduction),
      cp("netDeduction", r.netDeduction),
      cp("taxableOrdinaryIncome", r.taxableOrdinaryIncome),
      cp("taxOnOrdinaryIncome", r.taxOnOrdinaryIncome),
      cp("taxOnQualifiedIncome", r.taxOnQualifiedIncome),
      cp("taxSlopeForOrdinaryIncome", r.taxSlopeForOrdinaryIncome),
      cp("taxSlopeForQualifiedIncome", r.taxSlopeForQualifiedIncome),
      cp("taxSlopeForSocSec", r.taxSlopeForSocSec),
      cp("taxDue", r.taxDue)
    )
  end tf_tax_results

  /** The marginal tax rate on ordinary income. Example:
    * TF_TAX_SLOPE_ORDINARY(0.34, 2023, "Single", 1955-10-02, 0, 10000, 40000, 5000,
    * 0)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year a year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {number} the marginal tax rate as a factor
    */
  @JSExportTopLevel("tf_tax_slope_ordinary")
  def tf_tax_slope_ordinary(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    personalExemptions: Input,
    socSec: Input,
    ordinaryIncomeNonSS: Input,
    qualifiedIncome: Input,
    itemizedDeductions: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .calculator
      .results(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .taxSlopeForOrdinaryIncome

  end tf_tax_slope_ordinary

  /** The marginal tax rate on ordinary income for a specified delta. Example:
    * TF_TAX_SLOPE_ORDINARY_DELTA(0.34, 2023, "Single", 1955-10-02, 0, 10000, 40000, 5000,
    * 0, -2000)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year a year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @param {number} delta the delta to ordinary income
    * @returns {number} the marginal tax rate as a factor
    */
  @JSExportTopLevel("tf_tax_slope_ordinary_delta")
  def tf_tax_slope_ordinary_delta(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    personalExemptions: Input,
    socSec: Input,
    ordinaryIncomeNonSS: Input,
    qualifiedIncome: Input,
    itemizedDeductions: Input,
    ordinaryIncomeNonSSDelta: Input
  ): Output =
    val start =
      math.min(
        ordinaryIncomeNonSS: Double,
        (ordinaryIncomeNonSS: Double) + (ordinaryIncomeNonSSDelta: Double)
      )
    val end =
      math.max(
        ordinaryIncomeNonSS: Double,
        (ordinaryIncomeNonSS: Double) + (ordinaryIncomeNonSSDelta: Double)
      )

    val taxDueAtStart: Double = tf_tax_due(
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
    val taxDueAtEnd: Double = tf_tax_due(
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
    (taxDueAtEnd - taxDueAtStart) / math.abs(ordinaryIncomeNonSSDelta: Double)
  end tf_tax_slope_ordinary_delta

  /** The marginal tax rate on ordinary income. Example:
    * TF_TAX_SLOPE_QUALIFIED(0.34, 2023, "Single", 1955-10-02, 0, 10000, 40000, 5000,
    * 0)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year a year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {number} the marginal tax rate as a factor
    */
  @JSExportTopLevel("tf_tax_slope_qualified")
  def tf_tax_slope_qualified(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    personalExemptions: Input,
    socSec: Input,
    ordinaryIncomeNonSS: Input,
    qualifiedIncome: Input,
    itemizedDeductions: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .calculator
      .results(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .taxSlopeForQualifiedIncome
  end tf_tax_slope_qualified

  /** The marginal tax rate on Social Security income. Example:
    * TF_TAX_SLOPE_SOCSEC(0.34, 2023, "Single", 1955-10-02, 0, 10000, 40000, 5000,
    * 0)
    *
    * @param {number} bracketInflationRate estimate of future tax bracket inflation, e.g. 2%
    * @param {number} year a year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} personalExemptions self plus dependents, only relevant in a PreTCJA year
    * @param {number} socSec Social Security benefits received
    * @param {number} ordinaryIncomeNonSS ordinary income excluding Social Security
    * @param {number} qualifiedIncome qualified dividends and long term capital gains
    * @param {number} itemizedDeductions total of any itemized deductions
    * @returns {number} the marginal tax rate as a factor
    */
  @JSExportTopLevel("tf_tax_slope_socsec")
  def tf_tax_slope_socsec(
    bracketInflationRate: Input,
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    personalExemptions: Input,
    socSec: Input,
    ordinaryIncomeNonSS: Input,
    qualifiedIncome: Input,
    itemizedDeductions: Input
  ): Output =
    val bracketInflationFactor = 1.0 + (bracketInflationRate: Double)
    BoundRegime
      .forAnyYear(year, bracketInflationFactor, filingStatus)
      .calculator
      .results(
        birthDate,
        personalExemptions,
        socSec,
        ordinaryIncomeNonSS,
        qualifiedIncome,
        itemizedDeductions
      )
      .taxSlopeForSocSec
  end tf_tax_slope_socsec

  /** The MA state income tax due.
    * Example: TS_MA_TAX_DUE(2022, "Married", 1955-10-02, 0, 130000)
    *
    * @param {number} year a year between 2016 and the current year
    * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
    * @param {object} birthDate tax payer's date of birth
    * @param {number} dependents
    * @param {number} massachusettsGrossIncome
    * @returns {number} the MA state income tax due.
    */
  @JSExportTopLevel("ts_ma_tax_due")
  def ts_ma_tax_due(
    year: Input,
    filingStatus: Input,
    birthDate: Input,
    dependents: Input,
    massachusettsGrossIncome: Input
  ): Output =
    StateMATaxCalculator.taxDue(year, filingStatus, birthDate, dependents)(
      massachusettsGrossIncome
    )
  end ts_ma_tax_due

end Facade
