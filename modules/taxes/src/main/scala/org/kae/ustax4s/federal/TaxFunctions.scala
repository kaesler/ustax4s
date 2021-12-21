package org.kae.ustax4s.federal

import org.kae.ustax4s.money.{Income, TaxPayable, TaxableIncome}
import org.kae.ustax4s.taxfunction.TaxFunction

object TaxFunctions:
  def taxDueOnOrdinaryIncome(brackets: OrdinaryIncomeBrackets)(
    taxableOrdinaryIncome: TaxableIncome
  ): TaxPayable =
    TaxFunction.fromBrackets(brackets.brackets)(taxableOrdinaryIncome)

  def taxDueOnQualifiedIncome(brackets: QualifiedIncomeBrackets)(
    taxableOrdinaryIncome: TaxableIncome,
    qualifiedIncome: TaxableIncome
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
