package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.IntMoneySyntax
import org.kae.ustax4s.inretirement.MyTaxInRetirement

object TaxDueAdHocTests extends App with IntMoneySyntax {

  val res = MyTaxInRetirement.federalTaxDue(
    year = Year.of(2021),
    socSec = 0.tm,
    ordinaryIncomeNonSS = (20150 + 1400).tm,
    qualifiedIncome = 52700.tm
  )
  println(res)
}
