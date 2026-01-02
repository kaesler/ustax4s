package org.kae.ustax4s.federal

import java.time.LocalDate
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome}

final case class FederalCalcInput(
  birthDate: LocalDate,
  personalExemptions: Int,
  socSec: Income,
  ordinaryIncomeNonSS: Income,
  qualifiedIncome: TaxableIncome,
  itemizedDeductions: Deduction = Deduction.zero,
  aotcEligibleTuition: Deduction = Deduction.zero
):
  def withMoreSocSec(inc: Income): FederalCalcInput = copy(socSec = socSec + inc)
  def withLessSocSec(dec: Income): FederalCalcInput = copy(
    socSec = socSec.reduceBy(dec)
  )

  def withMoreOrdinaryIncome(inc: Income): FederalCalcInput = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS + inc
  )
  def withLessOrdinaryIncome(dec: Income): FederalCalcInput = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS.reduceBy(dec)
  )

  def withMoreQualifiedIncome(inc: TaxableIncome): FederalCalcInput = copy(
    qualifiedIncome = qualifiedIncome + inc
  )
  def withLessQualifiedIncome(dec: TaxableIncome): FederalCalcInput = copy(
    qualifiedIncome = qualifiedIncome.reduceBy(dec)
  )
end FederalCalcInput
