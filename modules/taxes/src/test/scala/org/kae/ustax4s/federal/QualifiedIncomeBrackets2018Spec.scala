package org.kae.ustax4s.federal

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.federal.QualifiedIncomeBrackets
import org.kae.ustax4s.money.Money

class QualifiedIncomeBrackets2018Spec extends FunSuite:
  import org.kae.ustax4s.MoneyConversions.given

  test(
    "QualifiedIncomeTaxBrackets for HOH 2018 should match my actual return"
  ) {
    val brackets = QualifiedIncomeBrackets.of(Year.of(2018), HeadOfHousehold)
    assertEquals(
      brackets
        .taxDue(
          114547,
          14777
        )
        .rounded,
      Money(2217)
    )
  }
