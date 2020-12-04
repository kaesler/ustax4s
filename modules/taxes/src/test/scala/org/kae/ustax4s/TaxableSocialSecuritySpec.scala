package org.kae.ustax4s

import org.specs2.ScalaCheck
import org.specs2.matcher.MustMatchers
import org.specs2.mutable.Specification

object TaxableSocialSecuritySpec
  extends Specification
    with ScalaCheck
    with TMoneyGeneration
    with MustMatchers
    with IntMoneySyntax {

  "Example 1 from Pub 915" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      28900.tm,
      5980.tm
    ) === 2990.tm
  }

  "Jackson Example from Pub 915" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      25500.tm,
      11000.tm
    ) === 3000.tm
  }

  "Example like I will face" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      17000.tm,
      49000.tm
    ) === 10875.tm
  }
  // TODO: Test at each boundary with zero ord income

  // TODO: Test at each boundary with huge ord income
}
