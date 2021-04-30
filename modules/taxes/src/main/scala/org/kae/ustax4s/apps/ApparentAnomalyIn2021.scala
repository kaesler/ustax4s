package org.kae.ustax4s.apps

import java.time.Year

import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s._

object ApparentAnomalyIn2021 extends App with IntMoneySyntax {
  val year = Year.of(2021)

  val ss                   = 0.tm
  val earnedIncome         = 32661.tm
  val unqualifiedDividends = 1400.tm
  val qualifiedDividends   = 5600.tm

  val from401k = 34689.tm.+(32150.tm)

  val tax =
    SimpleTaxInRetirement.taxDueUsingForm1040(
      year = year,
      filingStatus = HeadOfHousehold,
      socSec = ss,
      incomeFrom401k = from401k + unqualifiedDividends,
      qualifiedDividends = qualifiedDividends,
      verbose = false
    )
  println(s"tax: $tax")
}
