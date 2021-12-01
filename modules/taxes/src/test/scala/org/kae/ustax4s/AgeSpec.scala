package org.kae.ustax4s

import java.time.{LocalDate, Year}
import munit.FunSuite

class AgeSpec extends FunSuite:
  test("Age.isAge65OrOlder works correctly") {
    this.assert(
      Age.isAge65OrOlder(
        birthDate = LocalDate.of(1955, 10, 2),
        taxYear = Year.of(2020)
      )
    )
    this.assert(
      Age.isAge65OrOlder(
        birthDate = LocalDate.of(1956, 1, 1),
        taxYear = Year.of(2020)
      )
    )
    this.assert(
      !Age.isAge65OrOlder(
        birthDate = LocalDate.of(1955, 10, 2),
        taxYear = Year.of(2019)
      )
    )
  }
end AgeSpec
