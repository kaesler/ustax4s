package org.kae.ustax4s.money

import cats.Monoid
import cats.implicits.*
import org.kae.ustax4s.SourceLoc
import scala.annotation.targetName

export Moneys.Deduction
export Moneys.Income
export Moneys.IncomeThreshold
export Moneys.TaxableIncome
export Moneys.TaxCredit
export Moneys.TaxPayable

/** Module for various types for money. They are all distinct opaque types over Money. Money in turn
  * is an opaque type over BigDecimal with a restricted repertoire of operations. All have a Monoid
  * which is the addition Monoid Cats provides for BigDecimal. Money itself has a CommutativeMonoid
  * enhanced with the monus operator, subtraction truncated so the result is non-negative. Some of
  * the important functions defined: applyAdjustments: (Income, List[Deduction]) -> Income
  * applyDeductions: (Income, List[Deduction]) -> TaxableIncome TaxFunction: TaxableIncome ->
  * TaxPayable applyCredits: (TaxPayable, List[TaxCredit]) -> TaxPayable
  */
object Moneys:

  opaque type Income = Money
  object Income:
    val zero: Income                   = Money.zero
    def apply(i: Int): Income          = Money(i)
    def apply(d: Double): Income       = Money(d)
    def unsafeParse(s: String): Income = Money.unsafeParse(s)

    given Monoid[Income]   = summonAdditionMonoid
    given Ordering[Income] = summonOrdering

    extension (left: Income)
      def asDouble: Double         = left.toDouble
      def +(right: Income): Income = left.combine(right)

      infix def amountAbove(threshold: IncomeThreshold): Income =
        Money.monus(left, threshold)

      infix def applyAdjustments(deductions: Deduction*): Income =
        Money.monus(left, deductions.combineAll)

      infix def applyDeductions(deductions: Deduction*): TaxableIncome =
        Money.monus(left, deductions.combineAll)

      infix def div(right: Income): Double = Money.divide(left, right)

      infix def divInt(right: Int): Income = Money.divide(left, right)

      infix def inflateBy(d: Double): Income = Money.multiply(left, d)

      infix def isBelow(threshold: IncomeThreshold): Boolean =
        summon[Ordering[Income]].lt(left, threshold)

      def isZero: Boolean = Money.isZero(left)

      infix def mul(d: Double): Income = Money.multiply(left, d)

      infix def reduceBy(right: Income): Income = Money.monus(left, right)
    end extension
  end Income

  opaque type Deduction = Money
  object Deduction:
    val zero: Deduction                   = Money.zero
    def apply(i: Int): Deduction          = Money(i)
    def apply(d: Double): Deduction       = Money(d)
    def unsafeParse(s: String): Deduction = Money.unsafeParse(s)

    given Monoid[Deduction]   = summonAdditionMonoid
    given Ordering[Deduction] = summonOrdering

    extension (left: Deduction)
      inline def asDouble: Double               = left.toDouble
      def +(right: Deduction): Deduction        = left.combine(right)
      infix def inflateBy(d: Double): Deduction = Money.multiply(left, d)
      infix def mul(i: Int): Deduction          = Money.multiply(left, i)
    end extension
  end Deduction

  opaque type IncomeThreshold <: TaxableIncome = Money
  object IncomeThreshold:
    val zero: IncomeThreshold          = Money.zero
    def apply(i: Int): IncomeThreshold = Money(i)

    given Monoid[IncomeThreshold] = summonAdditionMonoid

    extension (left: IncomeThreshold)
      infix def asIncome: Income = left
      infix def absoluteDifference(right: IncomeThreshold): TaxableIncome =
        Money.absoluteDifference(left, right)

      def rounded1: IncomeThreshold = Money.rounded(left)

      infix def increaseBy(factor: Double): IncomeThreshold =
        require(factor > 1.0, SourceLoc())
        Money.multiply(left, factor).rounded
  end IncomeThreshold

  opaque type TaxableIncome <: Income = Money
  object TaxableIncome:
    val zero: TaxableIncome                   = Money.zero
    def apply(d: Double): TaxableIncome       = Money(d)
    def apply(i: Int): TaxableIncome          = Money(i)
    def unsafeParse(s: String): TaxableIncome = Money.unsafeParse(s)

    given Monoid[TaxableIncome] = summonAdditionMonoid

    extension (left: TaxableIncome)
      def asDouble: Double                       = left.toDouble
      def +(right: TaxableIncome): TaxableIncome = left.combine(right)

      infix def amountAbove(threshold: IncomeThreshold): TaxableIncome =
        Money.monus(left, threshold)

      def isZero: Boolean = Money.isZero(left)

      infix def reduceBy(right: TaxableIncome): TaxableIncome =
        Money.monus(left, right)

      infix def taxAt[T: TaxRate](rate: T): TaxPayable = Money.multiply(left, rate.asFraction)
    end extension
  end TaxableIncome

  opaque type TaxPayable = Money
  object TaxPayable:
    val zero: TaxPayable                   = Money.zero
    def apply(i: Int): TaxPayable          = Money(i)
    def apply(d: Double): TaxPayable       = Money(d)
    def unsafeParse(s: String): TaxPayable = Money.unsafeParse(s)

    given Monoid[TaxPayable]   = summonAdditionMonoid
    given Ordering[TaxPayable] = summonOrdering

    extension (left: TaxPayable)
      def asDouble: Double                 = left.toDouble
      def +(right: TaxPayable): TaxPayable = left.combine(right)

      infix def absoluteDifference(right: TaxPayable): TaxPayable =
        Money.absoluteDifference(left, right)

      infix def applyCredits(cs: TaxCredit*): TaxPayable =
        Money.monus(left, cs.combineAll)

      infix def div(i: Int): TaxPayable = Money.divide(left, i)
      def isZero: Boolean               = left == zero

      infix def isCloseTo(right: TaxPayable, tolerance: Int): Boolean =
        Money.areClose(left, right, tolerance)

      def nonZero: Boolean    = !isZero
      def rounded: TaxPayable = Money.rounded(left)

      infix def reduceBy(right: TaxPayable): TaxPayable =
        Money.monus(left, right)
    end extension
  end TaxPayable

  opaque type TaxCredit = Money
  object TaxCredit:
    val zero: TaxCredit          = Money.zero
    def apply(i: Int): TaxCredit = Money(i)

    given Monoid[TaxCredit] = summonAdditionMonoid

    extension (left: TaxCredit)
      @targetName("combine")
      def +(right: TaxCredit): TaxCredit = left.combine(right)
  end TaxCredit

  // Note: must be outside the object scopes above.
  private def summonAdditionMonoid = summon[Monoid[Money]]
  private def summonOrdering       = summon[Ordering[Money]]
end Moneys
