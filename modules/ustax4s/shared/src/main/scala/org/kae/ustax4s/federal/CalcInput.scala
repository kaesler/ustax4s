package org.kae.ustax4s.federal

import java.time.LocalDate
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome}

// TODO: Maybe just CalcInput.
// We may need to include state-relevant stuff
//   - pensions not taxed by state
//   - SocSec
final case class CalcInput(
  birthDate: LocalDate,
  personalExemptions: Int,
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
