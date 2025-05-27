package org.kae.ustax4s.federal

import org.kae.ustax4s.SourceLoc
import org.kae.ustax4s.money.TaxRate

opaque type FederalTaxRate = Double

object FederalTaxRate:

  def unsafeFrom(d: Double): FederalTaxRate =
    require(d >= 0.0, SourceLoc.loc)
    require(d <= 0.40, SourceLoc.loc)
    d

  given TaxRate[FederalTaxRate]:
    override def absoluteDifference(
      left: FederalTaxRate,
      right: FederalTaxRate
    ): FederalTaxRate = (left - right).abs
    override def toDouble(r: FederalTaxRate): Double = r
    override val zero                                = 0.0

end FederalTaxRate
