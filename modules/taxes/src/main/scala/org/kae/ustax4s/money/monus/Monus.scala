package org.kae.ustax4s.money.monus

import cats.implicits.*
import cats.kernel.{CommutativeMonoid, Group}

// See https://en.wikipedia.org/wiki/Monus
trait Monus[A] extends CommutativeMonoid[A]:
  def subtractTruncated(left: A, right: A): A
end Monus

object Monus extends MonusOps:

  // The natural Monus on functions returning B, when B has a Monus.
  given [A, B](using mb: Monus[B]): Monus[A => B] with
    type F = A => B
    def empty: F                         = { _ => mb.empty }
    def combine(f: F, g: F): F           = { a => f(a).combine(g(a)) }
    def subtractTruncated(f: F, g: F): F = { a => f(a) subt g(a) }

  // Provides a Monus for many types.
  given [A](using group: Group[A], ordering: Ordering[A]): Monus[A] with
    def empty: A                      = group.empty
    def combine(left: A, right: A): A = group.combine(left, right)
    def subtractTruncated(left: A, right: A): A =
      if ordering.lt(right, left) then group.remove(left, right)
      else empty
end Monus

trait MonusOps:
  extension [A: Monus](left: A)
    infix def subt(right: A): A = summon[Monus[A]].subtractTruncated(left, right)
end MonusOps
