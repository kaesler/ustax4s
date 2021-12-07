package org.kae.ustax4s.moneys

import cats.Monoid
import cats.implicits.*
import org.kae.ustax4s.money.Money

opaque type Income = Money

// TODO: Improve type safety.
// e.g. Income and Deduction could be separate semigroups,
// allowing only addition.
// Then an operation to subtract a deduction from an income,
// with result constrained to be non-negative.
// TaxPayable as another Semigroup?
// Also need to be able to:
//    - multiply Income by a positive fraction
//    - find the ratio of two incomes?
// Or: phantom types? Money & AsIncome, Money & AsDeduction
// Also: look at Haskell code for taxes.
// https://github.com/frasertweedale/hs-tax/blob/master/src/Data/Tax.hs
// https://hackage.haskell.org/package/tax-0.2.0.0/docs/Data-Tax.html

object Income:

  // TODO: make further distinctions:
  //   OrdinaryIncome
  //   QualifiedIncome (and threshold)
  //   TaxableIncome
  //   SocialSecurityIncome

  given Monoid[Income] = summonMonoid

  extension (underlying: Income)
    def +(right: Income): Income             = underlying.combine(right)
    def deduct(deduction: Deduction): Income = deduction.subtractFrom(underlying)
    def amountAbove(threshold: IncomeThreshold): Income =
      threshold.subtractFrom(underlying: Money)
  end extension

end Income
// Avoid infinite recursion by placing outside the Money object.
private def summonMonoid = summon[Monoid[Money]]