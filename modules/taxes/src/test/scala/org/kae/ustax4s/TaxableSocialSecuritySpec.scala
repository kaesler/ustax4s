package org.kae.ustax4s

import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus.Single

class TaxableSocialSecuritySpec extends ScalaCheckSuite with TMoneyGeneration with IntMoneySyntax {

  test("Untaxable 1") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 50000.tm,
        ssRelevantOtherIncome = 0.tm
      ),
      0.tm
    )
  }

  test("Untaxable 2") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 40000.tm,
        ssRelevantOtherIncome = 5000.tm
      ),
      0.tm
    )
  }

  test("Top of middle tier 1") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 68000.tm,
        ssRelevantOtherIncome = 0.tm
      ),
      4500.tm
    )
  }

  test("Top of middle tier 2") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 28000.tm,
        ssRelevantOtherIncome = 20000.tm
      ),
      4500.tm
    )
  }

  test("Example 1 from Pub 915") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 5980.tm,
        ssRelevantOtherIncome = 28900.tm
      ),
      2990.tm
    )
  }

  test("Jackson Example from Pub 915") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 11000.tm,
        ssRelevantOtherIncome = 25500.tm
      ),
      3000.tm
    )
  }

  test("Example like I will face") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 49000.tm,
        ssRelevantOtherIncome = 17000.tm
      ),
      10875.tm
    )
  }
}
