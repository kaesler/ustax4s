package org.kae.ustax4s.calculator

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome}
import org.kae.ustax4s.states.State

// TODO
// We may need to include state-relevant stuff
//   - pensions not taxed by state
//   - SocSec
//   - should FilingStatus be here?
final case class CalcInput(
  year: Year,
  estimatedAnnualInflationFactor: Double,
  state: State,
  filingStatus: FilingStatus,
  birthDate: LocalDate,
  personalExemptions: Int,
  //
  socSec: Income,
  ordinaryIncomeNonSS: Income,
  qualifiedIncome: TaxableIncome,
  itemizedDeductions: Deduction = Deduction.zero,
  aotcEligibleTuition: Deduction = Deduction.zero
):
  def withMoreSocSec(inc: Income): CalcInput = copy(socSec = socSec + inc)
  def withLessSocSec(dec: Income): CalcInput = copy(
    socSec = socSec.reduceBy(dec)
  )

  def withMoreOrdinaryIncome(inc: Income): CalcInput = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS + inc
  )
  def withLessOrdinaryIncome(dec: Income): CalcInput = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS.reduceBy(dec)
  )

  def withMoreQualifiedIncome(inc: TaxableIncome): CalcInput = copy(
    qualifiedIncome = qualifiedIncome + inc
  )
  def withLessQualifiedIncome(dec: TaxableIncome): CalcInput = copy(
    qualifiedIncome = qualifiedIncome.reduceBy(dec)
  )
end CalcInput
