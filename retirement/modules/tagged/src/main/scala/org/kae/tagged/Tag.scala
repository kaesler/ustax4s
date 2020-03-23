package org.kae.tagged

import cats.Eq
import cats.Order

/**
  * Newtype mechanism allowing for true unboxed value types,
  * inspired from shapeless
  */
trait Tag[U]

// $COVERAGE-OFF$
object Tag {

  type @@[+A, B] = A with Tag[B]
  implicit def eqForTagged[A, T]: Eq[A @@ T] = Eq.fromUniversalEquals[A @@ T]
  implicit def orderForTaggedComparable[A <: Comparable[A], T]: Order[A @@ T] =
    Order.from { (left: A, right: A) =>
      left.compareTo(right)
    }
  implicit def orderForTagged[A: Ordering, T]: Order[A @@ T] =
    Order.from((a1, a2) => implicitly[Ordering[A]].compare(a1, a2))

  def untag[A, B](ab: A @@ B): A = ab

  trait Tagged[A, B] extends (A => A @@ B) with TaggedSyntax {
    final def apply(a: A): A @@ B = a.asInstanceOf[A @@ B]
    final def unapply(arg: A @@ B): Option[A] = Some(arg)
    implicit def orderForTagged(implicit o: Order[A]): Order[A @@ B] =
      (x: A @@ B, y: A @@ B) => o.compare(untag(x), untag(y))
  }
}
