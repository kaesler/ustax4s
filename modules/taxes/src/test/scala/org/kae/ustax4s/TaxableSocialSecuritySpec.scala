package org.kae.ustax4s

import org.kae.ustax4s.FilingStatus.Single
import org.specs2.ScalaCheck
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object TaxableSocialSecuritySpec
    extends Specification
    with ScalaCheck
    with TMoneyGeneration
    with MustMatchers
    with IntMoneySyntax {

  "Untaxable 1" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      Single,
      0.tm,
      50000.tm
    ) === 0.tm
  }

  "Untaxable 2" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      Single,
      5000.tm,
      40000.tm
    ) === 0.tm
  }

  "Top of middle tier 1" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      Single,
      0.tm,
      68000.tm
    ) === 4500.tm
  }

  "Top of middle tier 2" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      Single,
      20000.tm,
      28000.tm
    ) === 4500.tm
  }

  "Example 1 from Pub 915" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      Single,
      28900.tm,
      5980.tm
    ) === 2990.tm
  }

  "Jackson Example from Pub 915" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      Single,
      25500.tm,
      11000.tm
    ) === 3000.tm
  }

  "Example like I will face" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      Single,
      17000.tm,
      49000.tm
    ) === 10875.tm
  }
}
