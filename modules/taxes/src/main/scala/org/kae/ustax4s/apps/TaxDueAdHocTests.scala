package org.kae.ustax4s.apps

import cats.implicits.*
import java.time.Year
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.{FilingStatus, IntMoneySyntax, Kevin}

object TaxDueAdHocTests extends App with IntMoneySyntax:

  val results = TaxInRetirement.federalTaxResults(
    year = Year.of(2021),
    Kevin.birthDate,
    FilingStatus.HeadOfHousehold,
    socSec = 17332.asMoney,
    ordinaryIncomeNonSS = 14250.asMoney,
    qualifiedIncome = 47963.asMoney
  )

  println(results.show)
