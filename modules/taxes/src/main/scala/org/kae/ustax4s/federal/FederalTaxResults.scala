package org.kae.ustax4s.federal

import cats.Show
import org.kae.ustax4s.money.Money

final case class FederalTaxResults(
  ssRelevantOtherIncome: Money,
  taxableSocialSecurity: Money,
  personalExemptionDeduction: Money,
  standardDeduction: Money,
  netDeduction: Money,
  taxableOrdinaryIncome: Money,
  taxOnOrdinaryIncome: Money,
  taxOnQualifiedIncome: Money
):
  def taxDue: Money = taxOnOrdinaryIncome + taxOnQualifiedIncome

object FederalTaxResults:
  given Show[FederalTaxResults] = (r: FederalTaxResults) =>
    val b = StringBuilder()
    b.append("Outputs\n")
    import r.*
    b.append(s"  ssRelevantOtherIncome: $ssRelevantOtherIncome\n")
    b.append(s"  taxableSocSec: $taxableSocialSecurity\n")
    b.append(s"  personalExceptionDeduction: $personalExemptionDeduction\n")
    b.append(s"  standardDeduction: $standardDeduction\n")
    b.append(s"  netDeduction: $netDeduction\n")
    b.append(s"  taxableOrdinaryIncome: $taxableOrdinaryIncome\n")
    b.append(s"  taxOnOrdinaryIncome: $taxOnOrdinaryIncome\n")
    b.append(s"  taxOnQualifiedIncome: $taxOnQualifiedIncome\n")
    b.append(s"  taxDue: $taxDue\n")

    b.result
