package org.kae.ustax4s.federal.forms

import cats.implicits.*
import java.time.Year
import munit.FunSuite
import org.kae.ustax4s.federal.{BoundRegime, TaxFunctions, Trump}
import org.kae.ustax4s.kevin.Kevin
import org.kae.ustax4s.money.{Income, TaxCredit, TaxPayable}
import org.kae.ustax4s.taxfunction.TaxFunction

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
        foreignTaxCredit = TaxCredit(257)
      ).some,
      schedule4 = Schedule4(
        selfEmploymentTax = TaxPayable(56)
      ).some,
      schedule5 = Schedule5(
        excessSocialSecurityWithheld = TaxCredit(1709)
      ).some,
      childTaxCredit = TaxCredit(2000),
      wages = Income(133497),
      taxExemptInterest = Income(2294),
      taxableInterest = Income(0),
      ordinaryDividends = Income(7930),
      qualifiedDividends = Income(7365),
      taxableIraDistributions = Income(0),
      socialSecurityBenefits = Income(0)
    )

    assertEquals(form.totalIncome, Income(150919))
    assertEquals(form.adjustedGrossIncome, Income(147324))
    assertEquals(form.taxableIncome, Income(129324))

    assertEquals(form.taxableOrdinaryIncome, Income(114547))
    assertEquals(form.qualifiedIncome, Income(14777))

    val taxOnInv =
      TaxFunctions.taxDueOnQualifiedIncome(
        regime
          .qualifiedIncomeBrackets(year, filingStatus)
      )(
        form.taxableOrdinaryIncome,
        form.qualifiedIncome
      )
        .rounded
    assertEquals(taxOnInv, TaxPayable(2217))

    val taxOnOrd =
      TaxFunction.fromBrackets(
        regime
          .ordinaryIncomeBrackets(year, filingStatus).brackets
      )(form.taxableOrdinaryIncome)
        .rounded
    assertEquals(taxOnOrd, TaxPayable(20389))

    assertEquals(
      Form1040
        .taxDueBeforeCredits(
          form.taxableOrdinaryIncome,
          form.qualifiedIncome,
          ordinaryIncomeBrackets,
          qualifiedIncomeBrackets
        )
        .rounded,
      TaxPayable(22606)
    )
    assertEquals(
      Form1040
        .totalFederalTax(
          form,
          ordinaryIncomeBrackets,
          qualifiedIncomeBrackets
        )
        .rounded,
      TaxPayable(20405)
    )
  }
