package org.kae.ustax4s.federal

import cats.Show
import org.kae.ustax4s.money.Moneys.{Deduction, Income, TaxPayable, TaxableIncome}
import scala.collection.mutable

trait FederalCalcResults:

  def taxableSocialSecurity: Income
  def agi: Income
  def personalExemptionDeduction: Deduction
  def standardDeduction: Deduction
  def meansTestedSeniorDeduction: Deduction
  def netDeduction: Deduction
  def taxableOrdinaryIncome: TaxableIncome
  def taxOnOrdinaryIncome: TaxPayable
  def taxOnQualifiedIncome: TaxPayable
  def taxSlopeForOrdinaryIncome: Double
  def taxSlopeForQualifiedIncome: Double
  def taxSlopeForSocSec: Double

  def taxDue: TaxPayable = taxOnOrdinaryIncome + taxOnQualifiedIncome
end FederalCalcResults

object FederalCalcResults:
  given Show[FederalCalcResults] = (r: FederalCalcResults) =>
    val b = mutable.StringBuilder()
    b.append("Outputs\n")
    import r.*
    // b.append(s"  ssRelevantOtherIncome: $ssRelevantOtherIncome\n")
    b.append(s"  taxableSocSec: $taxableSocialSecurity\n")
    b.append(s"  personalExceptionDeduction: $personalExemptionDeduction\n")
    // b.append(s"  unadjustedStandardDeduction: $unadjustedStandardDeduction\n")
    // b.append(s"  adjustmentWhenOver65: $adjustmentWhenOver65\n")
    // b.append(s"  adjustmentWhenOldAndSingle: $adjustmentWhenOldAndSingle\n")
    b.append(s"  standardDeduction: $standardDeduction\n")
    b.append(s"  netDeduction: $netDeduction\n")
    b.append(s"  taxableOrdinaryIncome: $taxableOrdinaryIncome\n")
    b.append(s"  taxOnOrdinaryIncome: $taxOnOrdinaryIncome\n")
    b.append(s"  taxOnQualifiedIncome: $taxOnQualifiedIncome\n")
    b.append(s"  taxDue: $taxDue\n")

    b.result
end FederalCalcResults
