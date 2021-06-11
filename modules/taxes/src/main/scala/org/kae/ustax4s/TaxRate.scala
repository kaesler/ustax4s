package org.kae.ustax4s

// Type class
trait TaxRate[T] extends Ordering[T]:
  extension (t: T) def asFraction: Double

  def compare(left: T, right: T): Int =
    left.asFraction.compare(right.asFraction)
end TaxRate
