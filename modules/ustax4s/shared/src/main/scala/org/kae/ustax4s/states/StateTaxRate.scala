package org.kae.ustax4s.states

import org.kae.ustax4s.{SourceLoc, TaxRate}

opaque type StateTaxRate = Double

object StateTaxRate:

  def unsafeFrom(d: Double): StateTaxRate =
    if d >= 0.0 && d < 1.0 then d
    else throw OutOfRange(d, SourceLoc())

  given TaxRate[StateTaxRate]:
    override def absoluteDifference(
      left: StateTaxRate,
      right: StateTaxRate
    ): StateTaxRate = (left - right).abs
    override def toDouble(r: StateTaxRate): Double = r
    override val zero                              = 0.0

  private final case class OutOfRange(
    d: Double,
    sourceLoc: String
  ) extends RuntimeException(
        s"Value is out of range for StateTaxRate: $d ($sourceLoc)"
      )

end StateTaxRate
