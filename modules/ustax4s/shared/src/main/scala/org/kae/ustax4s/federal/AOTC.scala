package org.kae.ustax4s.federal

import org.kae.ustax4s.money.Moneys.{Deduction, TaxCredit, RefundableTaxCredit}

object AOTC:

  //Maximum Credit: Up to $2500 annually per eligible student.
  // Calculation: It covers 100 % of the first  $2000 in qualified expenses
  // and  25 % of the next $2000 in expenses.
  // Refundability: Up to 40 % of the credit
  // (a maximum of $1, 000) may be refundable
  //, meaning you can receive money back even
  // if you owe no taxes
  // Phase out:
  // Income limitations apply: for 2025, the full credit is available
  // for individuals with a modified adjusted gross income (MAGI)
  // of $80,000 or less, or married couples filing jointly with a MAGI
  // of $160,000 or less.
  // The credit is reduced (phased out) for incomes above these
  // limits.
  //
  // Single, Head of Household, or Qualifying Surviving Spouse filers:
  // The credit is reduced if MAGI is between $80,000 and $90,000.
  // You cannot claim the credit if your MAGI is $90,000 or more.
  // Married couples filing jointly:
  // The credit is reduced if MAGI is between $160,000 and $180,000.
  // You cannot claim the credit if your MAGI is $180,000 or more.

  //
  // TODO:
  //  is applied to totalTax
  //  produces netTax? which can be negative (i.e. a refund0
  def apply(aotcEligibleTuition: Deduction):
  (totalCredit: TaxCredit, refundable: RefundableTaxCredit) = {
    ???
  }
end AOTC


