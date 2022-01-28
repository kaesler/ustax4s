package org.kae.ustax4s.federal

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.federal.QualifiedBrackets
import org.kae.ustax4s.money.TaxPayable

class QualifiedBrackets2018Spec extends FunSuite:
  import org.kae.ustax4s.money.MoneyConversions.given

  test(
    "QualifiedIncomeTaxBrackets for HOH 2018 should match my actual return"
  ) {
    val brackets = QualifiedBrackets.of(Year.of(2018), HeadOfHousehold)
    assertEquals(
      TaxFunctions.taxDueOnQualifiedIncome(brackets)(
        114547,
        14777
      ).rounded,
      TaxPayable(2217)
    )
  }
