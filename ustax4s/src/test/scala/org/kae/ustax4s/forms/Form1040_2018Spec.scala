package org.kae.ustax4s.forms

import java.time.Year
import org.kae.ustax4s.FilingStatus.HeadOfHousehold
import org.kae.ustax4s.{Kevin, TMoney, TaxRates}
import org.specs2.mutable.Specification

class Form1040_2018Spec extends Specification {

  val tm = TMoney.u _
  implicit class IntOps(i: Int) {
    def tm: TMoney = TMoney.u(i)
  }

  "Form1040" >> {
    "totalTax should match what I filed" >> {
      val year = Year.of(2018)
      val form = Form1040(
        Schedule1(
          businessIncomeOrLoss = 2080.tm,
          capitalGainOrLoss = 7412.tm,
          healthSavingsAccountDeduction = 3567.tm,
          deductiblePartOfSelfEmploymentTax = 28.tm
        ),
        Schedule3(
          foreignTaxCredit = 257.tm
        ),
        Schedule4(
          selfEmploymentTax = 56.tm
        ),
        wages = 133497.tm,
        taxableInterest = TMoney.zero,
        ordinaryDividends = 7930.tm,
        taxableIras = TMoney.zero,
        taxableSocialSecurityBenefits = TMoney.zero,
        rates = TaxRates.of(
          year,
          Kevin.filingStatus(year),
          Kevin.birthDate
        )
      )
      form.totalIncome === 150919.tm
      form.adjustedGrossIncome === 147324.tm
      form.taxableIncome === 129324.tm
      //form.tax === 22_606.tm
      //form.totalTax === 20_405.tm
      ok
    }
  }

}
