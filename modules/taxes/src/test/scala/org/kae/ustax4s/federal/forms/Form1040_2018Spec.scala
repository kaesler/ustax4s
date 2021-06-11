package org.kae.ustax4s.federal.forms

import cats.implicits.*
import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.moneyold.*
import org.kae.ustax4s.federal.TaxRates
import org.kae.ustax4s.kevin.Kevin

class Form1040_2018Spec extends FunSuite:

  test("Form1040 totalTax should match what I filed") {
    val year = Year.of(2018)
    val rates = TaxRates.of(
      year,
      Kevin.filingStatus(year),
      Kevin.birthDate
    )
    val form = Form1040(
      Kevin.filingStatus(year),
      standardDeduction = rates.standardDeduction,
      schedule1 = Schedule1(
        Some(
          ScheduleD(
            longTermCapitalGains = 5265.asMoney,
            capitalGainsDistributions = 2147.asMoney
          )
        ),
        businessIncomeOrLoss = 2080.asMoney,
        healthSavingsAccountDeduction = 3567.asMoney,
        deductiblePartOfSelfEmploymentTax = 28.asMoney
      ).some,
      schedule3 = Schedule3(
        foreignTaxCredit = 257.asMoney
      ).some,
      schedule4 = Schedule4(
        selfEmploymentTax = 56.asMoney
      ).some,
      schedule5 = Schedule5(
        excessSocialSecurityWithheld = 1709.asMoney
      ).some,
      childTaxCredit = 2000.asMoney,
      wages = 133497.asMoney,
      taxExemptInterest = 2294.asMoney,
      taxableInterest = TMoney.zero,
      ordinaryDividends = 7930.asMoney,
      qualifiedDividends = 7365.asMoney,
      taxableIraDistributions = TMoney.zero,
      socialSecurityBenefits = TMoney.zero,
      rates = rates
    )

    assertEquals(form.totalIncome, 150919.asMoney)
    assertEquals(form.adjustedGrossIncome, 147324.asMoney)
    assertEquals(form.taxableIncome, 129324.asMoney)

    assertEquals(form.taxableOrdinaryIncome, 114547.asMoney)
    assertEquals(form.qualifiedIncome, 14777.asMoney)

    val taxOnInv = rates.qualifiedIncomeBrackets
      .taxDueFunctionally(form.taxableOrdinaryIncome, form.qualifiedIncome)
      .rounded
    assertEquals(taxOnInv, 2217.asMoney)

    val taxOnOrd =
      rates.ordinaryIncomeBrackets.taxDue(form.taxableOrdinaryIncome).rounded
    assertEquals(taxOnOrd, 20389.asMoney)

    assertEquals(
      rates
        .taxDueBeforeCredits(
          form.taxableOrdinaryIncome,
          form.qualifiedIncome
        )
        .rounded,
      22606.asMoney
    )
    assertEquals(rates.totalTax(form).rounded, 20405.asMoney)
  }
