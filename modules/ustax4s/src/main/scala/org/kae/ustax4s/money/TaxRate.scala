package org.kae.ustax4s.money

// Type class.
trait TaxRate[T] extends Ordering[T]:
  def absoluteDifference(left: T, right: T): T
  def toDouble(t: T): Double
  def zero: T
  override def compare(x: T, y: T): Int = toDouble(x).compare(toDouble(y))

  extension (left: T)
    def asFraction: Double       = toDouble(left)
    infix def delta(right: T): T = absoluteDifference(left, right)
  end extension
end TaxRate
