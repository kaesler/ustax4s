package org.kae.ustax4s.money.cmm

import cats.implicits.*
import cats.kernel.{CommutativeMonoid, Group}

// A commutative monoid with monus, a truncated subtraction.
// See https://en.wikipedia.org/wiki/Monus
trait CMM[A] extends CommutativeMonoid[A]:
  def monus(left: A, right: A): A
end CMM

object CMM extends CMMOps:

  // TODO: verify the laws are satisfied
  //   Monoid laws
  //   Commmutativity: a <> b = b <> a
  //   x <> (y - x) = y <> (x - y)
  //   (x - y) - z = x - (y <> z)
  //   x - x = mempty
  //   mempty - x = mempty

  // The natural Monus on functions returning B, when B has a Monus.
  given [A, B: {CMM as mb}] => CMM[A => B]:
    type F = A => B
    def empty: F               = { _ => mb.empty }
    def combine(f: F, g: F): F = { a => f(a).combine(g(a)) }
    def monus(f: F, g: F): F   = { a => f(a).monus(g(a)) }

  // Provides a CMM for many types via Cats.
  given [A: {Group as group, Ordering as ordering}] => CMM[A]:
    def empty: A                      = group.empty
    def combine(left: A, right: A): A = group.combine(left, right)
    def monus(left: A, right: A): A =
      if ordering.lt(right, left) then group.remove(left, right)
      else empty
end CMM

trait CMMOps:
  extension [A: CMM](left: A) infix def monus(right: A): A = summon[CMM[A]].monus(left, right)
end CMMOps
