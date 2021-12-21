package org.kae.ustax4s.state_ma

import org.kae.ustax4s.money.Income
import org.kae.ustax4s.taxfunction.TaxFunction

object TaxFunctions:
  def forStateTaxableIncome(rate: StateMATaxRate): TaxFunction =
    TaxFunction.makeFlatTax(rate: StateMATaxRate)
end TaxFunctions
