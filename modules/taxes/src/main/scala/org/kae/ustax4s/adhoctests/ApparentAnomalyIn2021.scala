package org.kae.ustax4s.adhoctests

import java.time.Year
import org.kae.ustax4s.calculator.MyTaxCalculator
import org.kae.ustax4s.money.Money

object ApparentAnomalyIn2021 extends App:
  val year = Year.of(2021)

  val ss                   = Money(0)
  val earnedIncome         = Money(32661)
  val unqualifiedDividends = Money(1400)
  val qualifiedDividends   = Money(5600)

  val from401k = Money(34689) + Money(32150)

  val tax =
    MyTaxCalculator.federalTaxDueUsingForm1040(
      year = year,
      socSec = ss,
      ordinaryIncomeNonSS = from401k + unqualifiedDividends,
      qualifiedDividends = qualifiedDividends,
      verbose = false
    )
  println(s"tax: $tax")
