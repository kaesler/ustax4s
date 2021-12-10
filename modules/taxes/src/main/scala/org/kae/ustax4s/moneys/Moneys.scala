package org.kae.ustax4s.moneys

import cats.implicits.*
import cats.Monoid
import cats.Show
import org.kae.ustax4s.money.Money

object Moneys:

  // TODO: An attempt to improve type safety.
  // Also need to be able to:
  //    - multiply Income by a positive fraction
  //    - find the ratio of two incomes?
  // Or: phantom types? Money & AsIncome, Money & AsDeduction
  // Also: look at Haskell code for taxes.
  // https://github.com/frasertweedale/hs-tax/blob/master/src/Data/Tax.hs
  // https://hackage.haskell.org/package/tax-0.2.0.0/docs/Data-Tax.html
  // TODO: make further distinctions:
  //   OrdinaryIncome
  //   QualifiedIncome
  //   SocialSecurityIncome
  //   Allow them to be added to form Income?
  //   OR use phantom types for those?
  //   Federal vs State

  opaque type Income = Money
  object Income:
    def apply(i: Int): Income    = Money(i)
    def apply(d: Double): Income = Money(d)

    given Monoid[Income]   = summonAdditionMonoid
    given Ordering[Income] = summonOrdering
    given Show[Income]     = summonShow

    extension (left: Income)
      def +(right: Income): Income                              = left.combine(right)
      infix def deduct(deduction: Deduction): TaxableIncome     = left subp deduction
      infix def amountAbove(threshold: IncomeThreshold): Income = left subp threshold
    end extension
  end Income

  opaque type Deduction = Money
  object Deduction:
    def apply(i: Int): Deduction    = Money(i)
    def apply(d: Double): Deduction = Money(d)

    given Monoid[Deduction]   = summonAdditionMonoid
    given Ordering[Deduction] = summonOrdering
    given Show[Deduction]     = summonShow

    extension (underlying: Deduction)
      def +(right: Deduction): Deduction =
        underlying.combine(right)
    end extension
  end Deduction

  opaque type IncomeThreshold = Money
  object IncomeThreshold:
    // Note: these are always integers.
    def apply(i: Int): IncomeThreshold = Money(i)

    given Ordering[IncomeThreshold] = summonOrdering
    given Show[IncomeThreshold]     = summonShow
  end IncomeThreshold

  opaque type TaxableIncome = Money
  object TaxableIncome:
    given Ordering[TaxableIncome] = summonOrdering
    given Show[TaxableIncome]     = summonShow
  end TaxableIncome

  // Note: The result of applying a tax rate to an Income.
  opaque type TaxPayable = Money
  object TaxPayable:
    def apply(i: Int): TaxPayable    = Money(i)
    def apply(d: Double): TaxPayable = Money(d)

    given Monoid[TaxPayable]   = summonAdditionMonoid
    given Ordering[TaxPayable] = summonOrdering
    given Show[TaxPayable]     = summonShow

    extension (underlying: TaxPayable)
      def +(right: TaxPayable): TaxPayable = underlying.combine(right)
    end extension
  end TaxPayable

  private def summonAdditionMonoid = summon[Monoid[Money]]
  private def summonShow           = summon[Show[Money]]
  private def summonOrdering       = summon[Ordering[Money]]

end Moneys
