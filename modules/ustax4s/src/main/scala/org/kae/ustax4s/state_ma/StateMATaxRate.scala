package org.kae.ustax4s.state_ma

import org.kae.ustax4s.money.TaxRate

opaque type StateMATaxRate = Double

object StateMATaxRate:

  def unsafeFrom(d: Double): StateMATaxRate =
    require(d >= 0.0)
    require(d <= 0.10)
    d

  given tr: TaxRate[StateMATaxRate]:
    override def absoluteDifference(
      left: StateMATaxRate,
      right: StateMATaxRate
    ): StateMATaxRate = (left - right).abs
    override def toDouble(r: StateMATaxRate): Double = r
    override val zero                                = 0.0

end StateMATaxRate
