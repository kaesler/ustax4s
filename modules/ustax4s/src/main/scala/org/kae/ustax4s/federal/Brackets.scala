package org.kae.ustax4s.federal

import cats.implicits.*
import cats.{Eq, PartialOrder}
import org.kae.ustax4s.money.{Income, IncomeThreshold}
import org.kae.ustax4s.TaxRate
import org.kae.ustax4s.money.IncomeThreshold
import scala.math.Ordered.orderingToOrdered

object Brackets:
  opaque type Brackets = [R] =>> Map[IncomeThreshold, R]

  extension [R](bs: Brackets[R])
    def thresholds: Set[IncomeThreshold] = bs.keySet
    def rates: Set[R]                    = bs.values.toSet
    def size: Int                        = bs.iterator.size

    def bracketsAscending: Vector[(IncomeThreshold, R)] =
      bs.iterator.toVector.sortBy(_._1: Income)

    def ratesAscending(using Ordering[R]): Vector[R] =
      bs.iterator.toVector.sorted.map(_._2)

    def isProgressive(using Ordering[R]): Boolean =
      ratesAscending.zip(ratesAscending.tail).forall(_ < _)

    // Adjust the bracket starts for inflation.
    // E.g. for 2% inflation: inflated(1.02)
    def inflatedBy(factor: Double): Brackets[R] =
      require(factor >= 1.0)
      bs.iterator.map { (start, rate) =>
        (start.increaseBy(factor).rounded, rate)
      }.toMap

  given [R]: PartialOrder[Brackets[R]] with
    def partialCompare(left: Brackets[R], right: Brackets[R]): Double =
      if areComparable(left, right) then
        val pairs = left.keys.zip(right.keys)

        // All equal
        if pairs.forall(_ == _) then 0.0

        // Some but not all greater
        else if pairs.forall(_ >= _) then 1.0

        // Some but not all smaller
        else if pairs.forall(_ <= _) then -1.0
        else Double.NaN
      else Double.NaN

  private def areComparable[R](left: Brackets[R], right: Brackets[R]): Boolean =
    left.size == right.size && left.rates == right.rates

end Brackets
