package org.kae.ustax4s.money.monus

import cats.kernel.{CommutativeMonoid, Group}

// See https://en.wikipedia.org/wiki/Monus
trait Monus[A] extends CommutativeMonoid[A]:
  def subtractTruncated(left: A, right: A): A
end Monus

object Monus:
  given [A](using group: Group[A], ordering: Ordering[A]): Monus[A] with
    def empty: A                      = group.empty
    def combine(left: A, right: A): A = group.combine(left, right)
    def subtractTruncated(left: A, right: A): A =
      if ordering.lt(right, left) then group.remove(left, right)
      else empty
end Monus
