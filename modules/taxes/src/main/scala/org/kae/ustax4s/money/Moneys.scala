package org.kae.ustax4s.money

import cats.implicits.*
import cats.{Monoid, Show}
import org.kae.ustax4s.TaxRate

export Moneys.Deduction
export Moneys.Income
export Moneys.IncomeThreshold
export Moneys.TaxableIncome
export Moneys.TaxCredit
export Moneys.TaxPayable

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
    val zero: Income                   = Money.zero
    def apply(i: Int): Income          = Money(i)
    def apply(d: Double): Income       = Money(d)
    def unsafeParse(s: String): Income = Money.unsafeParse(s)

    given Monoid[Income]   = summonAdditionMonoid
    given Ordering[Income] = summonOrdering
    given Show[Income]     = summonShow

    extension (left: Income)
      def +(right: Income): Income = left.combine(right)

      // TODO: move to TaxableIncome?
      infix def amountAbove(threshold: IncomeThreshold): Income =
        Money.subtractTruncated(left, threshold)

      infix def applyDeductions(deductions: Deduction*): Income =
        Money.subtractTruncated(left, deductions.combineAll)

      infix def div(right: Income): Double = Money.divide(left, right)

      infix def inflateBy(d: Double): Income = Money.multiply(left, d)

      infix def isBelow(threshold: IncomeThreshold): Boolean =
        summon[Ordering[Income]].lt(left, threshold)

      def isZero: Boolean = Money.isZero(left)

      // TODO: move to TaxableIncome?
      infix def taxAt[T: TaxRate](rate: T): TaxPayable = Money.taxAt(left, rate)

      infix def mul(d: Double): Income = Money.multiply(left, d)

      // TODO: can be flushed when we redo how we apply brackets.
      infix def reduceBy(right: Income): Income = Money.subtractTruncated(left, right)
    end extension
  end Income

  opaque type Deduction = Money
  object Deduction:
    val zero: Deduction                   = Money.zero
    def apply(i: Int): Deduction          = Money(i)
    def apply(d: Double): Deduction       = Money(d)
    def unsafeParse(s: String): Deduction = Money.unsafeParse(s)

    // Note: Monoid suffices.
    given Monoid[Deduction]   = summonAdditionMonoid
    given Ordering[Deduction] = summonOrdering
    given Show[Deduction]     = summonShow

    extension (left: Deduction)
      def +(right: Deduction): Deduction = left.combine(right)

      infix def inflateBy(d: Double): Deduction = Money.multiply(left, d)
      infix def mul(i: Int): Deduction          = Money.multiply(left, i)
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
      infix def absoluteDifference(right: IncomeThreshold): Income =
        Money.absoluteDifference(left, right)

      def asIncome: Income         = left
      def rounded: IncomeThreshold = Money.rounded(left)

      infix def increaseBy(factor: Double): IncomeThreshold =
        require(factor > 1.0)
        Money.multiply(left, factor).rounded
  end IncomeThreshold

  opaque type TaxableIncome = Money
  object TaxableIncome:
    // Note: Monoid suffices.
    given Monoid[TaxableIncome]   = summonAdditionMonoid
    given Ordering[TaxableIncome] = summonOrdering
    given Show[TaxableIncome]     = summonShow

    extension (left: TaxableIncome)
      infix def amountAbove(threshold: IncomeThreshold): TaxableIncome =
        Money.subtractTruncated(left, threshold)

      infix def taxAt[T: TaxRate](rate: T): TaxPayable = Money.taxAt(left, rate)
    end extension
  end TaxableIncome

  // Note: The result of applying a tax rate to an Income.
  opaque type TaxPayable = Money
  object TaxPayable:
    val zero: TaxPayable                   = Money.zero
    def apply(i: Int): TaxPayable          = Money(i)
    def apply(d: Double): TaxPayable       = Money(d)
    def unsafeParse(s: String): TaxPayable = Money.unsafeParse(s)

    // Note: Monoid suffices.
    given Monoid[TaxPayable]   = summonAdditionMonoid
    given Ordering[TaxPayable] = summonOrdering
    given Show[TaxPayable]     = summonShow

    extension (left: TaxPayable)
      def +(right: TaxPayable): TaxPayable = left.combine(right)

      infix def absoluteDifference(right: TaxPayable): TaxPayable =
        Money.absoluteDifference(left, right)

      infix def applyCredits(cs: TaxCredit*): TaxPayable =
        Money.subtractTruncated(left, cs.combineAll)

      infix def div(i: Int): TaxPayable = Money.divide(left, i)

      def isZero: Boolean = left == zero

      infix def isCloseTo(right: TaxPayable, tolerance: Int): Boolean =
        Money.areClose(left, right, tolerance)

      def nonZero: Boolean = !isZero

      def rounded: TaxPayable = Money.rounded(left)

      infix def reduceBy(right: TaxPayable): TaxPayable =
        Money.subtractTruncated(left, right)

    end extension
  end TaxPayable

  opaque type TaxCredit = Money
  object TaxCredit:
    val zero: TaxCredit          = Money.zero
    def apply(i: Int): TaxCredit = Money(i)

    // Note: Monoid suffices.
    given Monoid[TaxCredit]   = summonAdditionMonoid
    given Ordering[TaxCredit] = summonOrdering
    given Show[TaxCredit]     = summonShow

    extension (left: TaxCredit) def +(right: TaxCredit): TaxCredit = left.combine(right)
  end TaxCredit

  private def summonAdditionMonoid = summon[Monoid[Money]]
  private def summonShow           = summon[Show[Money]]
  private def summonOrdering       = summon[Ordering[Money]]

end Moneys
