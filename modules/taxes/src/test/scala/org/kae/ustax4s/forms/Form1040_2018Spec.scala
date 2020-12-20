package org.kae.ustax4s.forms

import cats.implicits._
import java.time.Year
import org.kae.ustax4s.{IntMoneySyntax, Kevin, NonNegMoneyOps, TMoney, TaxRates}
import org.specs2.mutable.Specification

object Form1040_2018Spec extends Specification with IntMoneySyntax {

  "Form1040" >> {
    "totalTax should match what I filed" >> {
      val year = Year.of(2018)
      val rates = TaxRates.of(
        year,
        Kevin.filingStatus(year),
        Kevin.birthDate
      )
      val form = Form1040(
        Kevin.filingStatus(year),
        standardDeduction = rates.standardDeduction,
        schedule1 =  Schedule1(
          Some(
            ScheduleD(
              longTermCapitalGains = 5265.tm,
              capitalGainsDistributions = 2147.tm
            )
          ),
          businessIncomeOrLoss = 2080.tm,
          healthSavingsAccountDeduction = 3567.tm,
          deductiblePartOfSelfEmploymentTax = 28.tm
        ).some,
        schedule3 = Schedule3(
          foreignTaxCredit = 257.tm
        ).some,
        schedule4 = Schedule4(
          selfEmploymentTax = 56.tm
        ).some,
        schedule5 = Schedule5(
          excessSocialSecurityWithheld = 1709.tm
        ).some,
        childTaxCredit = 2000.tm,
        wages = 133497.tm,
        taxExemptInterest = 2294.tm,
        taxableInterest = TMoney.zero,
        ordinaryDividends = 7930.tm,
        qualifiedDividends = 7365.tm,
        taxableIras = TMoney.zero,
        socialSecurityBenefits = TMoney.zero,
        rates = rates
      )

      form.totalIncome === 150919.tm
      form.adjustedGrossIncome === 147324.tm
      form.taxableIncome === 129324.tm

      form.taxableOrdinaryIncome === 114547.tm
      form.qualifiedInvestmentIncome === 14777.tm

      val taxOnInv = rates.investmentIncomeBrackets
        .taxDue(form.taxableOrdinaryIncome, form.qualifiedInvestmentIncome)
        .rounded
      taxOnInv === 2217.tm

      val taxOnOrd =
        rates.ordinaryIncomeBrackets.taxDue(form.taxableOrdinaryIncome).rounded
      taxOnOrd === 20389.tm

      rates
        .taxDueBeforeCredits(
          form.taxableOrdinaryIncome,
          form.qualifiedInvestmentIncome
        )
        .rounded === 22606.tm
      rates.totalTax(form).rounded === 20405.tm
    }
  }
}
