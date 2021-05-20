package org.kae.ustax4s.apps

import cats.implicits._
import java.time.Year
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.{FilingStatus, IntMoneySyntax, Kevin}

object TaxDueAdHocTests extends App with IntMoneySyntax {

  val results = TaxInRetirement.federalTaxResults(
    year = Year.of(2021),
    Kevin.birthDate,
    FilingStatus.Single,
    socSec = 0.tm,
    ordinaryIncomeNonSS = 0.tm,
    qualifiedIncome = 50000.tm
  )

  println(results.show)
}
