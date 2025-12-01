package org.kae.ustax4s.state_ma

import org.kae.ustax4s.TaxFunction

object TaxFunctions:
  def forStateTaxableIncome(rate: StateMATaxRate): TaxFunction =
    TaxFunction.makeFlatTax(rate: StateMATaxRate)
end TaxFunctions
