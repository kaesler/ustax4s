package org.kae.ustax4s.adhoctests

import java.time.Year
import org.kae.ustax4s.money.TMoney
import org.kae.ustax4s.money.MoneySyntax.*
import org.kae.ustax4s.inretirement.MyTaxInRetirement

object ApparentAnomalyIn2021 extends App:
  val year = Year.of(2021)

  val ss                   = 0.asMoney
  val earnedIncome         = 32661.asMoney
  val unqualifiedDividends = 1400.asMoney
  val qualifiedDividends   = 5600.asMoney

  val from401k = 34689.asMoney.+(32150.asMoney)

  val tax =
    MyTaxInRetirement.federalTaxDueUsingForm1040(
      year = year,
      socSec = ss,
      ordinaryIncomeNonSS = from401k + unqualifiedDividends,
      qualifiedDividends = qualifiedDividends,
      verbose = false
    )
  println(s"tax: $tax")
