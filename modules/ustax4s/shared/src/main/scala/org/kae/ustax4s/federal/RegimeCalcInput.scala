package org.kae.ustax4s.federal

import java.time.LocalDate
import org.kae.ustax4s.money.Moneys.*

final case class RegimeCalcInput(
  birthDate: LocalDate,
  personalExemptions: Int,
  socSec: Income,
  ordinaryIncomeNonSS: Income,
  qualifiedIncome: TaxableIncome,
  itemizedDeductions: Deduction
):
  def withMoreSocSec(inc: Income): RegimeCalcInput = copy(socSec = socSec + inc)
  def withLessSocSec(dec: Income): RegimeCalcInput = copy(
    socSec = socSec.reduceBy(dec)
  )

  def withMoreOrdinaryIncome(inc: Income): RegimeCalcInput = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS + inc
  )
  def withLessOrdinaryIncome(dec: Income): RegimeCalcInput = copy(
    ordinaryIncomeNonSS = ordinaryIncomeNonSS.reduceBy(dec)
  )

  def withMoreQualifiedIncome(inc: TaxableIncome): RegimeCalcInput = copy(
    qualifiedIncome = qualifiedIncome + inc
  )
  def withLessQualifiedIncome(dec: TaxableIncome): RegimeCalcInput = copy(
    qualifiedIncome = qualifiedIncome.reduceBy(dec)
  )
end RegimeCalcInput
