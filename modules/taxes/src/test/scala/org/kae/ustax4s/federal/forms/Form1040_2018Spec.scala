package org.kae.ustax4s.federal.forms

import cats.implicits.*
import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.federal.{BoundRegime, Trump}
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.*
import org.kae.ustax4s.money.MoneySyntax.*

class Form1040_2018Spec extends FunSuite:
  private val regime       = Trump
  private val year         = Year.of(2018)
  private val filingStatus = Kevin.filingStatus(year)
  private val boundRegime = BoundRegime.create(
    regime,
    year,
    Kevin.birthDate,
    filingStatus,
    Kevin.personalExemptions(year)
  )

  test("Form1040 totalTax should match what I filed") {
    val standardDeduction       = boundRegime.standardDeduction
    val ordinaryIncomeBrackets  = boundRegime.ordinaryIncomeBrackets
    val qualifiedIncomeBrackets = boundRegime.qualifiedIncomeBrackets

    val form = Form1040(
      filingStatus,
      standardDeduction,
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
      taxableInterest = Money.zero,
      ordinaryDividends = 7930.asMoney,
      qualifiedDividends = 7365.asMoney,
      taxableIraDistributions = Money.zero,
      socialSecurityBenefits = Money.zero
    )

    assertEquals(form.totalIncome, 150919.asMoney)
    assertEquals(form.adjustedGrossIncome, 147324.asMoney)
    assertEquals(form.taxableIncome, 129324.asMoney)

    assertEquals(form.taxableOrdinaryIncome, 114547.asMoney)
    assertEquals(form.qualifiedIncome, 14777.asMoney)

    val taxOnInv = regime
      .qualifiedIncomeBrackets(year, filingStatus)
      .taxDueFunctionally(form.taxableOrdinaryIncome, form.qualifiedIncome)
      .rounded
    assertEquals(taxOnInv, 2217.asMoney)

    val taxOnOrd =
      regime
        .ordinaryIncomeBrackets(year, filingStatus)
        .taxDue(form.taxableOrdinaryIncome)
        .rounded
    assertEquals(taxOnOrd, 20389.asMoney)

    assertEquals(
      Form1040
        .taxDueBeforeCredits(
          form.taxableOrdinaryIncome,
          form.qualifiedIncome,
          ordinaryIncomeBrackets,
          qualifiedIncomeBrackets
        )
        .rounded,
      22606.asMoney
    )
    assertEquals(
      Form1040
        .totalFederalTax(
          form,
          ordinaryIncomeBrackets,
          qualifiedIncomeBrackets
        )
        .rounded,
      20405.asMoney
    )
  }
