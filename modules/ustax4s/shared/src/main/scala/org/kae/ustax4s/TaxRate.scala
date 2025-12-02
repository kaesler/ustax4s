package org.kae.ustax4s

// Note: Type class is used instead of subtyping because
// of the need for TaxRate.zero in TaxFunctions.rateDeltas
trait TaxRate[T] extends Ordering[T]:
  def absoluteDifference(left: T, right: T): T
  def toDouble(t: T): Double
  def zero: T

  override def compare(x: T, y: T): Int = toDouble(x).compare(toDouble(y))

  extension (left: T)
    def asDouble: Double         = toDouble(left)
    infix def delta(right: T): T = absoluteDifference(left, right)
  end extension
end TaxRate
