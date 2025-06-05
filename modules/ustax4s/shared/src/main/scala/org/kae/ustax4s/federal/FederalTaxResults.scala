package org.kae.ustax4s.federal

import cats.Show
import org.kae.ustax4s.money.{Deduction, Income, TaxPayable, TaxableIncome}
import scala.collection.mutable

final case class FederalTaxResults(
  ssRelevantOtherIncome: Income,
  taxableSocialSecurity: Income,
  personalExemptionDeduction: Deduction,
  unadjustedStandardDeduction: Deduction,
  adjustmentWhenOver65: Deduction,
  adjustmentWhenOldAndSingle: Deduction,
  standardDeduction: Deduction,
  netDeduction: Deduction,
  taxableOrdinaryIncome: TaxableIncome,
  taxOnOrdinaryIncome: TaxPayable,
  taxOnQualifiedIncome: TaxPayable
):
  def taxDue: TaxPayable = taxOnOrdinaryIncome + taxOnQualifiedIncome

object FederalTaxResults:
  given Show[FederalTaxResults] = (r: FederalTaxResults) =>
    val b = mutable.StringBuilder()
    b.append("Outputs\n")
    import r.*
    b.append(s"  ssRelevantOtherIncome: $ssRelevantOtherIncome\n")
    b.append(s"  taxableSocSec: $taxableSocialSecurity\n")
    b.append(s"  personalExceptionDeduction: $personalExemptionDeduction\n")
    b.append(s"  unadjustedStandardDeduction: $unadjustedStandardDeduction\n")
    b.append(s"  adjustmentWhenOver65: $adjustmentWhenOver65\n")
    b.append(s"  adjustmentWhenOldAndSingle: $adjustmentWhenOldAndSingle\n")
    b.append(s"  standardDeduction: $standardDeduction\n")
    b.append(s"  netDeduction: $netDeduction\n")
    b.append(s"  taxableOrdinaryIncome: $taxableOrdinaryIncome\n")
    b.append(s"  taxOnOrdinaryIncome: $taxOnOrdinaryIncome\n")
    b.append(s"  taxOnQualifiedIncome: $taxOnQualifiedIncome\n")
    b.append(s"  taxDue: $taxDue\n")

    b.result
end FederalTaxResults
