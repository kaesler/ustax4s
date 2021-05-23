package org.kae.ustax4s.apps

import cats.implicits._
import java.time.Year
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.{FilingStatus, IntMoneySyntax, Kevin}

object TaxDueAdHocTests extends App with IntMoneySyntax {

  val results = TaxInRetirement.federalTaxResults(
    year = Year.of(2021),
    Kevin.birthDate,
    FilingStatus.HeadOfHousehold,
    socSec = 17332.tm,
    ordinaryIncomeNonSS = 14250.tm,
    qualifiedIncome = 47963.tm
  )

  println(results.show)
}
