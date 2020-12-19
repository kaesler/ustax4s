package org.kae.ustax4s.apps

import org.kae.ustax4s.FilingStatus.{HeadOfHousehold, Single}
import org.kae.ustax4s.{IntMoneySyntax, TaxableSocialSecurity}
import org.kae.ustax4s._
object BumpAnalysis extends App with IntMoneySyntax{
  // for each filing status in HOH, Single
  //   - with SS = 49000
  //   - for each income from 0 to 60k in $100 steps
  //     - taxable income
  //     - bracket
  //     - slope  (delta tax due)/(delta income)
  // Try it in a spreadsheet
  val socialSecurityIncome = 49128.tm

  val filingStatus = Single // HeadOfHousehold
  val pairs = for {
    i <- 0 to 60000 by 100
    relevantIncome = i.tm
//    taxableSocialSecurity = TaxableSocialSecurity.taxableSocialSecurityBenefits(
//      relevantIncome,
//      socialSecurityIncome
//    )
    taxDue = SimpleTaxInRetirement.taxDueWithSS_2021(filingStatus, relevantIncome, socialSecurityIncome)
  } yield (relevantIncome, taxDue)
  val res = pairs.sliding(2)
    .toList
    .map { pairs =>
      val p0 = pairs(0)
      val p1 = pairs(1)
      val relevantIncome = p0._1
      val slope = (p1._2 - p0._2).value  / 100
      println(s"Income: $relevantIncome, slope: $slope")

    }
}
