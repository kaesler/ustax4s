package org.kae.ustax4s

// Type class.
trait TaxRate[T]:
  extension (t: T) def asFraction: Double
end TaxRate
