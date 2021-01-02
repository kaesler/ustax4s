package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.{IntMoneySyntax, SimpleTaxInRetirement}

object TaxDueAdHocTests extends App with IntMoneySyntax {

  val res = SimpleTaxInRetirement.taxDue(
    year = Year.of(2021),
    filingStatus = HeadOfHousehold,
    socSec = 0.tm,
    incomeFrom401kEtc = (20150 + 1400).tm,
    qualifiedInvestmentIncome = 52700.tm
  )
  println(res)
}
