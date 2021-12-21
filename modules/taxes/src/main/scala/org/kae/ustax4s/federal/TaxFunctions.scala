package org.kae.ustax4s.federal

import org.kae.ustax4s.money.{Income, TaxPayable}
import org.kae.ustax4s.taxfunction.TaxFunction

object TaxFunctions:
  def taxDueOnOrdinaryIncome(brackets: OrdinaryIncomeBrackets)(
    taxableOrdinaryIncome: Income
  ): TaxPayable =
    TaxFunction.fromBrackets(brackets.brackets)(taxableOrdinaryIncome)

  def taxDueOnQualifiedIncome(brackets: QualifiedIncomeBrackets)(
    taxableOrdinaryIncome: Income,
    qualifiedIncome: Income
  ): TaxPayable =
    // We use the qualified income brackets to compute the
    // tax on the sum of ordinaryIncome and qualifiedIncome,
    // and then subtract the tax it computed on ordinary
    // income because it is taxed using the (steeper) brackets for
    // ordinary income.
    val taxFunction = TaxFunction.fromBrackets(brackets.brackets)
    taxFunction(taxableOrdinaryIncome + qualifiedIncome)
      .reduceBy(taxFunction(taxableOrdinaryIncome))
end TaxFunctions
