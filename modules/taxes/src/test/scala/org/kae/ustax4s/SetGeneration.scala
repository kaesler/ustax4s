package org.kae.ustax4s

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.const
import org.scalacheck.{Arbitrary, Gen}
import scala.annotation.tailrec

/* ScalaCheck generators for sets. */
trait SetGeneration:

  /** Returns a Gen capable of producing a Set of elements of a specified size.
    * @tparam T
    *   a type for which there is an Arbitrary type class instance.
    * @param n
    *   the size of set desired
    * @param gen
    *   the [[Gen]] for T
    */
  def genSet[T](n: Int, gen: Gen[T]): Gen[Set[T]] = {

    @tailrec
    def loop(alreadyGenerated: Set[T]): Gen[Set[T]] =
      if alreadyGenerated.size == n then alreadyGenerated
      else
        val newOne =
          gen
            .retryUntil { t =>
              !alreadyGenerated.contains(t)
            }
            .sample
            .get
        loop(alreadyGenerated + newOne)

    loop(Set.empty)
  }

  /** Returns a Gen capable of producing a Set of elements of a specified size.
    * @tparam T
    *   a type for which there is an Arbitrary type class instance.
    * @param n
    *   the size of set desired
    */
  def genSetOfN[T: Arbitrary](n: Int): Gen[Set[T]] =
    genSet[T](n, arbitrary[T])

object SetGeneration extends SetGeneration
