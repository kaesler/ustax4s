package org.kae.ustax4s.adhoctests

import cats.implicits.*
import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.calculator.TaxCalculator
import org.kae.ustax4s.federal.{BoundRegime, FederalTaxCalculator, PreTrump, Trump}
import org.kae.ustax4s.money.{Deduction, Income, TaxableIncome}

object TaxDueAdHocTests extends App:

  import org.kae.ustax4s.MoneyConversions.given

  private val year                 = Year.of(2020)
  private val birthDate: LocalDate = LocalDate.of(1955, 10, 2)

  val results =
    BoundRegime
      .create(
        regime = Trump,
        year = year,
        filingStatus = FilingStatus.Single,
        birthDate = birthDate,
        personalExemptions = 1
      )
      .calculator
      .federalTaxResults(
        socSec = Income(0),
        ordinaryIncomeNonSS = Income(36024),
        qualifiedIncome = TaxableIncome(40828),
        itemizedDeductions = Deduction(11222)
      )

  println(results.show)
