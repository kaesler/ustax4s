package org.kae.ustax4s.federal

import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.federal.TaxableSocialSecurity
import org.kae.ustax4s.money.{Income, MoneyGeneration}

class TaxableSocialSecuritySpec extends ScalaCheckSuite with MoneyGeneration:
  import org.kae.ustax4s.money.MoneyConversions.given

  test("Untaxable 1") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Income(50000),
        ssRelevantOtherIncome = Income.zero
      ),
      Income.zero
    )
  }

  test("Untaxable 2") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Income(40000),
        ssRelevantOtherIncome = Income(5000)
      ),
      Income.zero
    )
  }

  test("Top of middle tier 1") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Income(68000),
        ssRelevantOtherIncome = Income.zero
      ),
      Income(4500)
    )
  }

  test("Top of middle tier 2") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Income(28000),
        ssRelevantOtherIncome = Income(20000)
      ),
      Income(4500)
    )
  }

  test("Example 1 from Pub 915") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Income(5980),
        ssRelevantOtherIncome = Income(28900)
      ),
      Income(2990)
    )
  }

  test("Jackson Example from Pub 915") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Income(11000),
        ssRelevantOtherIncome = Income(25500)
      ),
      Income(3000)
    )
  }

  test("Example like I will face") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Income(49000),
        ssRelevantOtherIncome = Income(17000)
      ),
      Income(10875)
    )
  }
