package org.kae.ustax4s.adhoctests

import cats.implicits.*
import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.federal.{FederalTaxCalculator, Trump}
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.Money
import org.kae.ustax4s.money.MoneySyntax.*

object TaxDueAdHocTests extends App:

  val year = Year.of(2021)
  val results =
    FederalTaxCalculator
      .create(
        baseRegime = Trump,
        year = year,
        Kevin.birthDate,
        FilingStatus.HeadOfHousehold,
        Kevin.personalExemptions(year)
      )
      .federalTaxResults(
        socSec = 17332,
        ordinaryIncomeNonSS = 14250,
        qualifiedIncome = 47963,
        itemizedDeductions = 0
      )()

  println(results.show)
