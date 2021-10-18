package org.kae.ustax4s.adhoctests

import cats.implicits.*
import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.Trump
import org.kae.ustax4s.inretirement.TaxInRetirement
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*

object TaxDueAdHocTests extends App:

  val results = TaxInRetirement.federalTaxResults(
    regime = Trump,
    year = Year.of(2021),
    Kevin.birthDate,
    FilingStatus.HeadOfHousehold,
    socSec = 17332.asMoney,
    ordinaryIncomeNonSS = 14250.asMoney,
    qualifiedIncome = 47963.asMoney
  )

  println(results.show)
