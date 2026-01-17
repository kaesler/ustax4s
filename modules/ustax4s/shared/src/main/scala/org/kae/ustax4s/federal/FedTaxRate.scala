package org.kae.ustax4s.federal

import org.kae.ustax4s.{SourceLoc, TaxRate}

opaque type FedTaxRate = Double

object FedTaxRate:

  def unsafeFrom(d: Double): FedTaxRate =
    if d >= 0.0 && d <= 0.40 then d
    else throw OutOfRange(d, SourceLoc())

  given TaxRate[FedTaxRate]:
    override val zero = 0.0
    override def absoluteDifference(
      left: FedTaxRate,
      right: FedTaxRate
    ): FedTaxRate = (left - right).abs

    override def toDouble(r: FedTaxRate): Double = r

  private final case class OutOfRange(
    d: Double,
    sourceLoc: String
  ) extends RuntimeException(
        s"Value is out of range for Federal tax rate: $d ($sourceLoc)"
      )

end FedTaxRate
