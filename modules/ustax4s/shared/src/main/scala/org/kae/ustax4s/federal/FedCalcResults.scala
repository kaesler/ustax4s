package org.kae.ustax4s.federal

import cats.Show
import java.time.{LocalDate, Year}
import org.kae.ustax4s.{FilingStatus, IncomeScenario}
import org.kae.ustax4s.money.NonNegMoneys.TaxRefundable
import org.kae.ustax4s.money.TaxOutcomes.TaxOutcome
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable, TaxableIncome}
import scala.collection.mutable

trait FedCalcResults:
  // Needed by State calcs
  def year: Year
  def filingStatus: FilingStatus
  def birthDate: LocalDate

  // The inputs to the calculation.
  def incomeScenario: IncomeScenario

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

  def taxPayable: TaxPayable = taxOnOrdinaryIncome + taxOnQualifiedIncome

  // TODO: Complete this so refundable credits can happen.
  def taxRefundable: TaxRefundable = TaxRefundable.zero
  def outcome: TaxOutcome          =
    TaxOutcome.ofPayable(taxPayable) + TaxOutcome.ofRefundable(taxRefundable)

end FedCalcResults

object FedCalcResults:
  given Show[FedCalcResults] = (r: FedCalcResults) =>
    val b = mutable.StringBuilder()
    b.append("Outputs\n")
    import r.*
    // b.append(s"  ssRelevantOtherIncome: $ssRelevantOtherIncome\n")
    b.append(s"  taxableSocSec: $taxableSocialSecurity\n")
    b.append(s"  agi: $agi\n")
    b.append(s"  personalExemptionDeduction: $personalExemptionDeduction\n")
    b.append(s"  standardDeduction: $standardDeduction\n")
    b.append(s"  meansTestedSeniorDeduction: $meansTestedSeniorDeduction\n")
    b.append(s"  netDeduction: $netDeduction\n")
    b.append(s"  taxableOrdinaryIncome: $taxableOrdinaryIncome\n")
    b.append(s"  taxOnOrdinaryIncome: $taxOnOrdinaryIncome\n")
    b.append(s"  taxOnQualifiedIncome: $taxOnQualifiedIncome\n")
    b.append(s"  taxDue: $taxPayable\n")
    b.append(s"  taxSlopeForOrdinaryIncome: $taxSlopeForOrdinaryIncome\n")
    b.append(s"  taxSlopeForQualifiedIncome: $taxSlopeForQualifiedIncome\n")

    b.result
end FedCalcResults
