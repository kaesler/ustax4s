package org.kae.ustax4s.federal

import cats.implicits.*
import munit.{Assertions, FunSuite}
import org.kae.ustax4s.Brackets
import org.kae.ustax4s.money.IncomeThreshold

class BracketsSpec extends FunSuite:

  test("PartialOrder[Brackets]") {
    val br1 = Brackets.of(
      List(
        0      -> 10d,
        9275   -> 15d,
        37650  -> 25d,
        91150  -> 28d,
        190150 -> 33d,
        413350 -> 35d,
        415050 -> 39.6d
      ).map((i, j) => (IncomeThreshold(i), j))
    )
    val br2 = Brackets.of(
      List(
        0      -> 10d,
        13250  -> 15d,
        50400  -> 25d,
        130150 -> 28d,
        210800 -> 33d,
        413350 -> 35d,
        441000 -> 39.6d
      ).map((i, j) => (IncomeThreshold(i), j))
    )
    Assertions.assert(
      br1 <= br2
    )
  }
end BracketsSpec
