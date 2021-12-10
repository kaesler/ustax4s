package org.kae.ustax4s.federal.forms

import cats.implicits.*
import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.federal.{BoundRegime, Trump}
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.*

class Form1040_2018Spec extends FunSuite:
  import org.kae.ustax4s.MoneyConversions.given

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
            longTermCapitalGains = 5265,
            capitalGainsDistributions = 2147
          )
        ),
        businessIncomeOrLoss = 2080,
        healthSavingsAccountDeduction = 3567,
        deductiblePartOfSelfEmploymentTax = 28
      ).some,
      schedule3 = Schedule3(
        foreignTaxCredit = 257
      ).some,
      schedule4 = Schedule4(
        selfEmploymentTax = Money(56)
      ).some,
      schedule5 = Schedule5(
        excessSocialSecurityWithheld = 1709
      ).some,
      childTaxCredit = 2000,
      wages = 133497,
      taxExemptInterest = 2294,
      taxableInterest = 0,
      ordinaryDividends = 7930,
      qualifiedDividends = 7365,
      taxableIraDistributions = 0,
      socialSecurityBenefits = 0
    )

    assertEquals(form.totalIncome, Money(150919))
    assertEquals(form.adjustedGrossIncome, Money(147324))
    assertEquals(form.taxableIncome, Money(129324))

    assertEquals(form.taxableOrdinaryIncome, Money(114547))
    assertEquals(form.qualifiedIncome, Money(14777))

    val taxOnInv = regime
      .qualifiedIncomeBrackets(year, filingStatus)
      .taxDueFunctionally(form.taxableOrdinaryIncome, form.qualifiedIncome)
      .rounded
    assertEquals(taxOnInv, Money(2217))

    val taxOnOrd =
      regime
        .ordinaryIncomeBrackets(year, filingStatus)
        .taxDue(form.taxableOrdinaryIncome)
        .rounded
    assertEquals(taxOnOrd, Money(20389))

    assertEquals(
      Form1040
        .taxDueBeforeCredits(
          form.taxableOrdinaryIncome,
          form.qualifiedIncome,
          ordinaryIncomeBrackets,
          qualifiedIncomeBrackets
        )
        .rounded,
      Money(22606)
    )
    assertEquals(
      Form1040
        .totalFederalTax(
          form,
          ordinaryIncomeBrackets,
          qualifiedIncomeBrackets
        )
        .rounded,
      Money(20405)
    )
  }
