package org.kae.ustax4s.federal

import org.kae.ustax4s.TaxFunction
import org.kae.ustax4s.money.{TaxPayable, TaxableIncome}

// TODO: Can these be be subsumed into BoundFedRegime?
// One less module?
object FedTaxFunctions:
  def taxPayableOnOrdinaryIncome(ordinaryRateFunction: OrdinaryRateFunction)(
    taxableOrdinaryIncome: TaxableIncome
  ): TaxPayable =
    TaxFunction.fromRateFunction(ordinaryRateFunction.rateFunction)(taxableOrdinaryIncome)

  def taxPayableOnQualifiedIncome(qualifiedRateFunction: QualifiedRateFunction)(
    taxableOrdinaryIncome: TaxableIncome,
    qualifiedIncome: TaxableIncome
  ): TaxPayable =
    // We use the qualified income brackets to compute the
    // tax on the sum of ordinaryIncome and qualifiedIncome,
    // and then subtract the tax it computed on ordinary
    // income because it is taxed using the (steeper) brackets for
    // ordinary income.
    val taxFunction = TaxFunction.fromRateFunction(qualifiedRateFunction.function)
    taxFunction(taxableOrdinaryIncome + qualifiedIncome)
      .monus(taxFunction(taxableOrdinaryIncome))
  end taxPayableOnQualifiedIncome

end FedTaxFunctions
