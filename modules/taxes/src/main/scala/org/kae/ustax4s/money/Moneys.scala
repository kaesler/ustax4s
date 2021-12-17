package org.kae.ustax4s.money

import cats.{Monoid, Show}
import cats.implicits.*
import org.kae.ustax4s.TaxRate

private[money] object Moneys:

  // TODO: look at Haskell code for taxes.
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
    def unsafeParse(s: String): Income = Money.unsafeParse(s)

    val zero: Income             = Money.zero
    def apply(i: Int): Income    = Money(i)
    def apply(d: Double): Income = Money(d)

    given Monoid[Income]   = summonAdditionMonoid
    given Ordering[Income] = summonOrdering
    given Show[Income]     = summonShow

    extension (left: Income)
      def +(right: Income): Income = left.combine(right)

      infix def div(right: Income): Double             = Money.divide(left, right)
      infix def taxAt[T: TaxRate](rate: T): TaxPayable = Money.taxAt(left, rate)
      def isZero: Boolean                              = Money.isZero(left)

      infix def mul(d: Double): Income = Money.multiply(left, d)
      infix def applyDeductions(deductions: Deduction*): Income =
        Money.subtractNonNegative(left, deductions.combineAll)
      infix def amountAbove(threshold: IncomeThreshold): Income =
        Money.subtractNonNegative(left, threshold)
      infix def isBelow(threshold: IncomeThreshold): Boolean =
        summon[Ordering[Income]].lt(left, threshold)

      infix def reduceBy(right: Income): Income = Money.subtractNonNegative(left, right)
      infix def inflateBy(d: Double): Income    = Money.multiply(left, d)

    end extension
  end Income

  opaque type Deduction = Money
  object Deduction:
    def unsafeParse(s: String): Deduction = Money.unsafeParse(s)

    val zero: Deduction             = Money.zero
    def apply(i: Int): Deduction    = Money(i)
    def apply(d: Double): Deduction = Money(d)

    given Monoid[Deduction]   = summonAdditionMonoid
    given Ordering[Deduction] = summonOrdering
    given Show[Deduction]     = summonShow

    extension (left: Deduction)
      def +(right: Deduction): Deduction = left.combine(right)

      infix def mul(i: Int): Deduction          = Money.multiply(left, i)
      infix def inflateBy(d: Double): Deduction = Money.multiply(left, d)

    end extension
  end Deduction

  opaque type IncomeThreshold = Money
  object IncomeThreshold:
    val zero: IncomeThreshold = Money.zero

    // Note: these are always integers.
    def apply(i: Int): IncomeThreshold = Money(i)

    given Ordering[IncomeThreshold] = summonOrdering
    given Show[IncomeThreshold]     = summonShow

    extension (left: IncomeThreshold)
      def rounded: IncomeThreshold = Money.rounded(left)

      def asIncome: Income = left
      infix def increaseBy(factor: Double): IncomeThreshold =
        require(factor > 1.0)
        Money.multiply(left, factor).rounded

      infix def absoluteDifference(right: IncomeThreshold): Income =
        Money.absoluteDifference(left, right)
  end IncomeThreshold

  opaque type TaxableIncome = Money
  object TaxableIncome:
    given Ordering[TaxableIncome] = summonOrdering
    given Show[TaxableIncome]     = summonShow
  end TaxableIncome

  // Note: The result of applying a tax rate to an Income.
  opaque type TaxPayable = Money
  object TaxPayable:
    def unsafeParse(s: String): TaxPayable = Money.unsafeParse(s)
    val zero: TaxPayable                   = Money.zero
    def apply(i: Int): TaxPayable          = Money(i)
    def apply(d: Double): TaxPayable       = Money(d)

    given Monoid[TaxPayable]   = summonAdditionMonoid
    given Ordering[TaxPayable] = summonOrdering
    given Show[TaxPayable]     = summonShow

    extension (left: TaxPayable)
      def +(right: TaxPayable): TaxPayable = left.combine(right)
      def isZero: Boolean                  = left == zero
      def nonZero: Boolean                 = !isZero

      infix def isCloseTo(right: TaxPayable, tolerance: Int): Boolean =
        Money.areClose(left, right, tolerance)

      infix def div(i: Int): TaxPayable = Money.divide(left, i)
      def rounded: TaxPayable           = Money.rounded(left)
      infix def applyCredits(cs: TaxCredit*): TaxPayable =
        Money.subtractNonNegative(left, cs.combineAll)
      infix def absoluteDifference(right: TaxPayable): TaxPayable =
        Money.absoluteDifference(left, right)
    end extension
  end TaxPayable

  opaque type TaxCredit = Money
  object TaxCredit:
    def apply(i: Int): TaxCredit                                   = Money(i)
    val zero: TaxCredit                                            = Money.zero
    given Monoid[TaxCredit]                                        = summonAdditionMonoid
    given Ordering[TaxCredit]                                      = summonOrdering
    given Show[TaxCredit]                                          = summonShow
    extension (left: TaxCredit) def +(right: TaxCredit): TaxCredit = left.combine(right)
  end TaxCredit

  private def summonAdditionMonoid = summon[Monoid[Money]]
  private def summonShow           = summon[Show[Money]]
  private def summonOrdering       = summon[Ordering[Money]]

end Moneys
export Moneys.Deduction
export Moneys.Income
export Moneys.IncomeThreshold
export Moneys.TaxableIncome
export Moneys.TaxCredit
export Moneys.TaxPayable
