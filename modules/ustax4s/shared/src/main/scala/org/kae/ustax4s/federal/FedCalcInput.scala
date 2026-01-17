package org.kae.ustax4s.federal

import java.time.LocalDate
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome}

// TODO: Maybe just CalcInput.
// We may need to include state-relevant stuff
//   - pensions not taxed by state
//   - SocSec
//   - should FilingStatus be here?
final case class FedCalcInput(
  // TODO: should these be in BoundRegime?
  birthDate: LocalDate,
  personalExemptions: Int,
  //
  socSec: Income,
  ordinaryIncomeNonSS: Income,
  qualifiedIncome: TaxableIncome,
  itemizedDeductions: Deduction = Deduction.zero,
  aotcEligibleTuition: Deduction = Deduction.zero
):
  def withMoreSocSec(inc: Income): FedCalcInput = copy(socSec = socSec + inc)
  def withLessSocSec(dec: Income): FedCalcInput = copy(
    socSec = socSec.reduceBy(dec)
  )

  def withMoreOrdinaryIncome(inc: Income): FedCalcInput = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS + inc
  )
  def withLessOrdinaryIncome(dec: Income): FedCalcInput = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS.reduceBy(dec)
  )

  def withMoreQualifiedIncome(inc: TaxableIncome): FedCalcInput = copy(
    qualifiedIncome = qualifiedIncome + inc
  )
  def withLessQualifiedIncome(dec: TaxableIncome): FedCalcInput = copy(
    qualifiedIncome = qualifiedIncome.reduceBy(dec)
  )
end FedCalcInput
