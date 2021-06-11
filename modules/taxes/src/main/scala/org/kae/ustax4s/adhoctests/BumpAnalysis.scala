package org.kae.ustax4s
package adhoctests

import java.time.Year
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.IntMoneySyntax
import org.kae.ustax4s.inretirement.MyTaxInRetirement

object BumpAnalysis extends App with IntMoneySyntax:
  // for each filing status in HOH, Single
  //   - with SS = 49000
  //   - for each income from 0 to 60k in $100 steps
  //     - taxable income
  //     - bracket
  //     - slope  (delta tax due)/(delta income)
  // Try it in a spreadsheet
  val socialSecurityIncome = 49128.asMoney

  val filingStatus = Single // HeadOfHousehold
  val pairs = for
    i <- 0 to 60000 by 100
    ssRelevantOtherIncome = i.asMoney
//    taxableSocialSecurity = TaxableSocialSecurity.taxableSocialSecurityBenefits(
//      relevantIncome,
//      socialSecurityIncome
//    )
    taxDue = MyTaxInRetirement.federalTaxDueUsingForm1040(
      year = Year.of(2021),
      socSec = socialSecurityIncome,
      ordinaryIncomeNonSS = ssRelevantOtherIncome,
      qualifiedDividends = 0.asMoney,
      verbose = false
    )
  yield (ssRelevantOtherIncome, taxDue)

  val res = pairs
    .sliding(2)
    .toList
    .map { pairs =>
      val p0                    = pairs(0)
      val p1                    = pairs(1)
      val ssRelevantOtherIncome = p0._1
      val slope                 = (p1._2 - p0._2).value / 100
      println(s"Income: $ssRelevantOtherIncome, slope: $slope")
    }