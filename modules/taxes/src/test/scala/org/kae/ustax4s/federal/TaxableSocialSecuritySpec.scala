package org.kae.ustax4s.federal

import munit.ScalaCheckSuite
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.federal.TaxableSocialSecurity
import org.kae.ustax4s.money.{Money, MoneyGeneration}

class TaxableSocialSecuritySpec extends ScalaCheckSuite with MoneyGeneration:
  import org.kae.ustax4s.MoneyConversions.given

  test("Untaxable 1") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = 50000,
        ssRelevantOtherIncome = 0
      ),
      Money(0)
    )
  }

  test("Untaxable 2") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Money(40000),
        ssRelevantOtherIncome = Money(5000)
      ),
      Money(0)
    )
  }

  test("Top of middle tier 1") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Money(68000),
        ssRelevantOtherIncome = Money(0)
      ),
      Money(4500)
    )
  }

  test("Top of middle tier 2") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Money(28000),
        ssRelevantOtherIncome = Money(20000)
      ),
      Money(4500)
    )
  }

  test("Example 1 from Pub 915") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Money(5980),
        ssRelevantOtherIncome = Money(28900)
      ),
      Money(2990)
    )
  }

  test("Jackson Example from Pub 915") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Money(11000),
        ssRelevantOtherIncome = Money(25500)
      ),
      Money(3000)
    )
  }

  test("Example like I will face") {
    assertEquals(
      TaxableSocialSecurity.taxableSocialSecurityBenefits(
        filingStatus = Single,
        socialSecurityBenefits = Money(49000),
        ssRelevantOtherIncome = Money(17000)
      ),
      Money(10875)
    )
  }
