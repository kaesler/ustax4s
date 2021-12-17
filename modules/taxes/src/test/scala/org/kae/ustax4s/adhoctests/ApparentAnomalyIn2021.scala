package org.kae.ustax4s.adhoctests

import java.time.Year
import org.kae.ustax4s.calculator.MyTaxCalculator
import org.kae.ustax4s.money.Income

object ApparentAnomalyIn2021 extends App:
  val year = Year.of(2021)

  val ss                   = Income(0)
  val earnedIncome         = Income(32661)
  val unqualifiedDividends = Income(1400)
  val qualifiedDividends   = Income(5600)

  val from401k = Income(34689) + Income(32150)

  val tax =
    MyTaxCalculator.federalTaxDueUsingForm1040(
      year = year,
      socSec = ss,
      ordinaryIncomeNonSS = from401k + unqualifiedDividends,
      qualifiedDividends = qualifiedDividends,
      verbose = false
    )
  println(s"tax: $tax")
