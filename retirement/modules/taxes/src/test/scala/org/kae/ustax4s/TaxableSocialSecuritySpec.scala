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
    ) === 2960.tm
  }

  "Jacskon Example from Pub 915" >> {
    TaxableSocialSecurity.taxableSocialSecurityBenefits(
      2550.tm,
      11000.tm
    ) === 3000.tm
  }
  // TODO: Test at each boundary with zero ord income

  // TODO: Test at each boundary with huge ord income
}