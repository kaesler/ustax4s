package org.kae.ustax4s

// Type class.
trait TaxRate[T]:
  extension (t: T) def asFraction: Double
  extension (t: T) def absoluteDifference(other: T): T

  extension (t: T)
    infix def <=(that: T): Boolean =
      t.asFraction <= that.asFraction

    infix def <(that: T): Boolean =
      t.asFraction < that.asFraction

  end extension

end TaxRate
