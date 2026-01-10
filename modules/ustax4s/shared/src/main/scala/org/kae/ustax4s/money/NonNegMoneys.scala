package org.kae.ustax4s.money

import cats.Monoid
import cats.implicits.*
import org.kae.ustax4s.{SourceLoc, TaxRate}
import scala.annotation.targetName

/** Module for various types for non-negative money.
  * They are all distinct opaque types
  *   - over NonNegativeMoney
  *   - which is opaque over Money,
  *   - which is opaque over BigDecimal.
  *
  * All have a Monoid which is the addition Monoid Cats provides for BigDecimal.
  *
  * Money itself has a CommutativeMonoid enhanced with the monus operator,
  * i.e. subtraction truncated so the result is non-negative.
  *
  *  Hierarchy:
  *    Money
  *      NonNegativeMoney
  *        Income
  *          TaxableIncome
  *            - after Deductions are applied
  *            - is a subtype so that we can consider that an instance
  *              "is-an" Income.
  *            IncomeThreshold
  *              - these appear in Bracket
  *        Deduction
  *        TaxCredit
  *          - not refundable, i.e. can only reduce tax payable to zero,
  *            and cannot produce a refund.
  *        RefundableTaxCredit
  *          - can produce a refund
  *        TaxPayable
  *          - amount of tax owed, if any
  *        TaxRefundable
  *           - amount of tax to be refunded, if any
  * Some of the important functions defined:
  *   - applyAdjustments: (Income, List[Deduction]) -> Income
  *   - applyDeductions: (Income, List[Deduction]) -> TaxableIncome
  *   - TaxFunction: TaxableIncome -> TaxPayable
  *   - applyCredits: (TaxPayable, List[TaxCredit]) -> TaxPayable
  *   - applyRefundableCredits:
  *     (TaxPayable, List[RefundableTaxCredit]) -> TaxPayable | TaxRefundable
  */
export NonNegMoneys.Deduction
export NonNegMoneys.Income
export NonNegMoneys.IncomeThreshold
export NonNegMoneys.RefundableTaxCredit
export NonNegMoneys.TaxableIncome
export NonNegMoneys.TaxCredit
export NonNegMoneys.TaxPayable
export NonNegMoneys.TaxRefundable

object NonNegMoneys:

  opaque type Income = NonNegativeMoney
  object Income:
    val zero: Income = NonNegativeMoney.zero

    def apply(i: Int): Income          = NonNegativeMoney(i)
    def apply(d: Double): Income       = NonNegativeMoney(d)
    def unsafeParse(s: String): Income = NonNegativeMoney.unsafeParse(s)

    given Monoid[Income]   = summonAdditionMonoid
    given Ordering[Income] = summonOrdering

    extension (left: Income)
      def isZero: Boolean  = NonNegativeMoney.isZero(left)
      def asDouble: Double = NonNegativeMoney.toDouble(left)

      def +(right: Income): Income = left.combine(right)

      infix def amountAbove(threshold: IncomeThreshold): Income =
        NonNegativeMoney.monus(left, threshold)
      infix def applyAdjustments(deductions: Deduction*): Income =
        NonNegativeMoney.monus(left, deductions.combineAll)
      infix def applyDeductions(deductions: Deduction*): TaxableIncome =
        NonNegativeMoney.monus(left, deductions.combineAll)

      infix def div(right: Income): Double = NonNegativeMoney.divide(left, right)
      infix def divInt(right: Int): Income = NonNegativeMoney.divide(left, right)

      infix def isBelow(threshold: IncomeThreshold): Boolean =
        summon[Ordering[Income]].lt(left, threshold)

      infix def mul(d: Double): Income          = NonNegativeMoney.multiply(left, d)
      infix def reduceBy(right: Income): Income = NonNegativeMoney.monus(left, right)
    end extension
  end Income

  opaque type Deduction = NonNegativeMoney
  object Deduction:
    val zero: Deduction = NonNegativeMoney.zero

    def apply(i: Int): Deduction          = NonNegativeMoney(i)
    def apply(d: Double): Deduction       = NonNegativeMoney(d)
    def unsafeParse(s: String): Deduction = NonNegativeMoney.unsafeParse(s)

    given Monoid[Deduction]   = summonAdditionMonoid
    given Ordering[Deduction] = summonOrdering

    extension (left: Deduction)
      inline def asDouble: Double               = NonNegativeMoney.toDouble(left)
      def +(right: Deduction): Deduction        = left.combine(right)
      infix def inflateBy(d: Double): Deduction = NonNegativeMoney.multiply(left, d)
      infix def mul(i: Int): Deduction          = NonNegativeMoney.multiply(left, i)
    end extension
  end Deduction

  // TODO: is this true?
  // Note: the subtyping means these types can't be opaque at the file level.
  // This is necessary so we can that a TaxableIncome "is-an" Income.
  opaque type TaxableIncome <: Income = NonNegativeMoney

  object TaxableIncome:
    val zero: TaxableIncome = NonNegativeMoney.zero

    def apply(d: Double): TaxableIncome       = NonNegativeMoney(d)
    def apply(i: Int): TaxableIncome          = NonNegativeMoney(i)
    def unsafeParse(s: String): TaxableIncome = NonNegativeMoney.unsafeParse(s)

    extension (left: TaxableIncome)
      def isZero: Boolean  = NonNegativeMoney.isZero(left)
      def asDouble: Double = NonNegativeMoney.toDouble(left)

      def +(right: TaxableIncome): TaxableIncome                       = left.combine(right)
      infix def amountAbove(threshold: IncomeThreshold): TaxableIncome =
        NonNegativeMoney.monus(left, threshold)
      infix def reduceBy(right: TaxableIncome): TaxableIncome =
        NonNegativeMoney.monus(left, right)
      infix def taxAt[T: TaxRate](rate: T): TaxPayable =
        NonNegativeMoney.multiply(left, rate.asDouble)
    end extension
  end TaxableIncome

  // Note: the subtyping means these types can't be opaque at the file level.
  // This is necessary so we can say that an IncomeThreshold "is-a" TaxableIncome.
  opaque type IncomeThreshold <: TaxableIncome = NonNegativeMoney
  object IncomeThreshold:
    val zero: IncomeThreshold = NonNegativeMoney.zero

    def apply(i: Int): IncomeThreshold = NonNegativeMoney(i)

    // Note: Only used in tests.
    given Monoid[IncomeThreshold] = summonAdditionMonoid

    extension (left: IncomeThreshold)
      def rounded: IncomeThreshold = NonNegativeMoney.rounded(left)

      infix def absoluteDifference(right: IncomeThreshold): TaxableIncome =
        NonNegativeMoney.absoluteDifference(left, right)

      infix def increaseBy(factor: Double): IncomeThreshold =
        require(factor > 1.0, SourceLoc())
        NonNegativeMoney.rounded(NonNegativeMoney.multiply(left, factor))
  end IncomeThreshold

  opaque type TaxCredit = NonNegativeMoney

  object TaxCredit:
    val zero: TaxCredit = NonNegativeMoney.zero

    def apply(i: Int): TaxCredit = NonNegativeMoney(i)

    given Monoid[TaxCredit] = summonAdditionMonoid

    extension (left: TaxCredit)
      @targetName("combine")
      def +(right: TaxCredit): TaxCredit = left.combine(right)
  end TaxCredit

  opaque type RefundableTaxCredit = NonNegativeMoney

  object RefundableTaxCredit:
    val zero: RefundableTaxCredit = NonNegativeMoney.zero

    def apply(i: Int): RefundableTaxCredit = NonNegativeMoney(i)

    given Monoid[RefundableTaxCredit] = summonAdditionMonoid

    extension (left: RefundableTaxCredit)
      @targetName("combine")
      def +(right: RefundableTaxCredit): RefundableTaxCredit = left.combine(right)
  end RefundableTaxCredit

  opaque type TaxPayable = NonNegativeMoney
  object TaxPayable:
    val zero: TaxPayable = NonNegativeMoney.zero

    def apply(i: Int): TaxPayable          = NonNegativeMoney(i)
    def apply(d: Double): TaxPayable       = NonNegativeMoney(d)
    def unsafeParse(s: String): TaxPayable = NonNegativeMoney.unsafeParse(s)

    given Monoid[TaxPayable]   = summonAdditionMonoid
    given Ordering[TaxPayable] = summonOrdering

    extension (left: TaxPayable)
      def isZero: Boolean     = NonNegativeMoney.isZero(left)
      def asDouble: Double    = NonNegativeMoney.toDouble(left)
      def nonZero: Boolean    = !isZero
      def rounded: TaxPayable = NonNegativeMoney.rounded(left)

      def +(right: TaxPayable): TaxPayable = left.combine(right)

      infix def applyNonRefundableCredits(cs: TaxCredit*): TaxPayable =
        NonNegativeMoney.monus(left, cs.combineAll)

      infix def applyRefundableCredits(cs: RefundableTaxCredit*): TaxPayable = {
        // left - cs.combineAll
        // TODO:
        //  First reduce TaxPayable as far as we can. THis produces TaxPayable.
        //  Then use the rest, if any, to produce TaxRefundable
        // Seems like the result is TaxOutcome?
        // is tax already PAID a refundable credit?
        // seems must be or TaxPayable needs to be negative
        //
        ???
      }
      infix def isCloseTo(right: TaxPayable, tolerance: Int): Boolean =
        NonNegativeMoney.areClose(left, right, tolerance)
      infix def reduceBy(right: TaxPayable): TaxPayable =
        NonNegativeMoney.monus(left, right)
    end extension
  end TaxPayable

  opaque type TaxRefundable = NonNegativeMoney

  object TaxRefundable:
    val zero: TaxRefundable                   = NonNegativeMoney.zero
    def apply(i: Int): TaxRefundable          = NonNegativeMoney(i)
    def apply(d: Double): TaxRefundable       = NonNegativeMoney(d)
    def unsafeParse(s: String): TaxRefundable = NonNegativeMoney.unsafeParse(s)

    given Monoid[TaxRefundable]   = summonAdditionMonoid
    given Ordering[TaxRefundable] = summonOrdering

    extension (left: TaxRefundable)
      infix def div(i: Int): TaxRefundable = NonNegativeMoney.divide(left, i)
      def isZero: Boolean                  = NonNegativeMoney.isZero(left)
      def asDouble: Double                 = NonNegativeMoney.toDouble(left)
      def asSigned: Double                 = -NonNegativeMoney.toDouble(left)
      def nonZero: Boolean                 = !isZero
      def rounded: TaxRefundable           = NonNegativeMoney.rounded(left)

      def +(right: TaxRefundable): TaxRefundable = left.combine(right)

      infix def reduceBy(right: TaxRefundable): TaxRefundable =
        NonNegativeMoney.monus(left, right)
    end extension
  end TaxRefundable

  // Note: must be outside the object scopes above.
  private def summonAdditionMonoid = summon[Monoid[NonNegativeMoney]]
  private def summonOrdering       = summon[Ordering[NonNegativeMoney]]
end NonNegMoneys
