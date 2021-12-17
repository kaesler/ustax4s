package org.kae.ustax4s.adhoctests

import cats.implicits.*
import java.time.Year
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.federal.{BoundRegime, FederalTaxCalculator, PreTrump, Trump}
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.{Deduction, Income}

object TaxDueAdHocTests extends App:

  import org.kae.ustax4s.MoneyConversions.given

  val year = Year.of(2020)
  val results =
    BoundRegime
      .create(
        regime = Trump,
        year = year,
        filingStatus = FilingStatus.Single,
        birthDate = Kevin.birthDate,
        personalExemptions = 1
      )
      .calculator
      .federalTaxResults(
        socSec = Income(0),
        ordinaryIncomeNonSS = Income(36024),
        qualifiedIncome = Income(40828),
        itemizedDeductions = Deduction(11222)
      )

  println(results.show)
