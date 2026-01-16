package org.kae.ustax4s.federal

import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.federal.yearly.YearlyValues
import org.kae.ustax4s.money.TaxPayable
import scala.language.implicitConversions

class QualifiedRateFunction2018Spec extends FunSuite:
  import org.kae.ustax4s.money.MoneyConversions.given

  test(
    "QualifiedIncomeTaxBrackets for HOH 2018 should match my actual return"
  ) {
    val qrf = YearlyValues.of(Year.of(2018))
      .get.qualifiedRateFunctions(HeadOfHousehold)
    assertEquals(
      FedTaxFunctions.taxPayableOnQualifiedIncome(qrf)(
        114547,
        14777
      ).rounded,
      TaxPayable(2217)
    )
  }
