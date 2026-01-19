package org.kae.ustax4s

import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome}

final case class IncomeScenario(
  socSec: Income,
  ordinaryIncomeNonSS: Income,
  qualifiedIncome: TaxableIncome,
  itemizedDeductions: Deduction = Deduction.zero,
  aotcEligibleTuition: Deduction = Deduction.zero
):
  def withMoreSocSec(inc: Income): IncomeScenario = copy(socSec = socSec + inc)
  def withLessSocSec(dec: Income): IncomeScenario = copy(
    socSec = socSec.reduceBy(dec)
  )

  def withMoreOrdinaryIncome(inc: Income): IncomeScenario = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS + inc
  )
  def withLessOrdinaryIncome(dec: Income): IncomeScenario = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS.reduceBy(dec)
  )

  def withMoreQualifiedIncome(inc: TaxableIncome): IncomeScenario = copy(
    qualifiedIncome = qualifiedIncome + inc
  )
  def withLessQualifiedIncome(dec: TaxableIncome): IncomeScenario = copy(
    qualifiedIncome = qualifiedIncome.reduceBy(dec)
  )
end IncomeScenario
