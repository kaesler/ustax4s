package org.kae.ustax4s.federal

import org.kae.ustax4s.SourceLoc
import org.kae.ustax4s.money.TaxRate

opaque type FederalTaxRate = Double

object FederalTaxRate:

  def unsafeFrom(d: Double): FederalTaxRate =
    if d >= 0.0 && d <= 0.40 then d
    else throw OutOfRange(d, SourceLoc())

  given TaxRate[FederalTaxRate]:
    override def absoluteDifference(
      left: FederalTaxRate,
      right: FederalTaxRate
    ): FederalTaxRate = (left - right).abs
    override def toDouble(r: FederalTaxRate): Double = r
    override val zero                                = 0.0

  private final case class OutOfRange(
    d: Double,
    sourceLoc: String
  ) extends RuntimeException(
        s"Value is out of range for Federal tax rate: $d ($sourceLoc)"
      )

end FederalTaxRate
