package org.kae.ustax4s.adhoctests

import cats.implicits.*
import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.{BoundRegime, FederalTaxCalculator, NonTrump, Trump}
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*

object TaxDueAdHocTests extends App:

  val year = Year.of(2017)
  val results =
    BoundRegime
      .create(
        regime = NonTrump,
        year = year,
        filingStatus = FilingStatus.HeadOfHousehold,
        birthDate = Kevin.birthDate,
        personalExemptions = 1
      )
      .calculator
      .federalTaxResults(
        socSec = 21961,
        ordinaryIncomeNonSS = 10169,
        qualifiedIncome = 0,
        itemizedDeductions = 0
      )

  println(results.show)
