package org.kae.ustax4s.federal

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.money.given
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.federal.QualifiedIncomeBrackets

class QualifiedIncomeBrackets2018Spec extends FunSuite:

  test(
    "QualifiedIncomeTaxBrackets for HOH 2018 should match my actual return"
  ) {
    val brackets = QualifiedIncomeBrackets.of(Year.of(2018), HeadOfHousehold)
    assertEquals(
      brackets
        .taxDueFunctionally(
          Money(114547),
          Money(14777)
        )
        .rounded,
      Money(2217)
    )
  }
