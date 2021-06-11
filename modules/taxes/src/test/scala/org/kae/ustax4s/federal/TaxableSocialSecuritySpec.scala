package org.kae.ustax4s.federal

import munit.ScalaCheckSuite
import org.kae.ustax4s.moneyold.*
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.federal.TaxableSocialSecurity

class TaxableSocialSecuritySpec extends ScalaCheckSuite with TMoneyGeneration:

  test("Untaxable 1") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 50000.asMoney,
        ssRelevantOtherIncome = 0.asMoney
      ),
      0.asMoney
    )
  }

  test("Untaxable 2") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 40000.asMoney,
        ssRelevantOtherIncome = 5000.asMoney
      ),
      0.asMoney
    )
  }

  test("Top of middle tier 1") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 68000.asMoney,
        ssRelevantOtherIncome = 0.asMoney
      ),
      4500.asMoney
    )
  }

  test("Top of middle tier 2") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 28000.asMoney,
        ssRelevantOtherIncome = 20000.asMoney
      ),
      4500.asMoney
    )
  }

  test("Example 1 from Pub 915") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 5980.asMoney,
        ssRelevantOtherIncome = 28900.asMoney
      ),
      2990.asMoney
    )
  }

  test("Jackson Example from Pub 915") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 11000.asMoney,
        ssRelevantOtherIncome = 25500.asMoney
      ),
      3000.asMoney
    )
  }

  test("Example like I will face") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 49000.asMoney,
        ssRelevantOtherIncome = 17000.asMoney
      ),
      10875.asMoney
    )
  }
