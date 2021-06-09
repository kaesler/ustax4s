package org.kae.ustax4s

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold

class QualifiedIncomeBrackets2018Spec extends FunSuite:

  test(
    "QualifiedIncomeTaxBrackets for HOH 2018 should match my actual return"
  ) {
    val brackets = QualifiedIncomeBrackets.of(Year.of(2018), HeadOfHousehold)
    assertEquals(
      brackets
        .taxDueFunctionally(
          TMoney(114547),
          TMoney(14777)
        )
        .rounded,
      TMoney(2217)
    )
  }
