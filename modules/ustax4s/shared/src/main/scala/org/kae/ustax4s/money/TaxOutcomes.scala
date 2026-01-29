package org.kae.ustax4s.money

import cats.Monoid
import cats.syntax.all.*

// Type for the ultimate outcome for State or Federal, or both.
export TaxOutcomes.TaxOutcome
object TaxOutcomes:
  import org.kae.ustax4s.money.NonNegMoneys.{TaxPayable, TaxRefundable}

  opaque type TaxOutcome = Either[TaxPayable, TaxRefundable]

  object TaxOutcome:
    val zero: TaxOutcome = ofPayable(TaxPayable.zero)

    def ofPayable(tp: TaxPayable): TaxOutcome       = tp.asLeft
    def ofRefundable(tr: TaxRefundable): TaxOutcome = tr.asRight

    extension (self: TaxOutcome)
      def +(other: TaxOutcome): TaxOutcome =
        summon[Monoid[TaxOutcome]].combine(self, other)

      def applyNonRefundableCredits(credits: TaxCredit*): TaxOutcome =
        val netCredit = credits.combineAll
        self match
          case Left(taxPayable)              => Left(taxPayable reduceByCredit netCredit)
          case refund @ Right(taxRefundable) => refund
      end applyNonRefundableCredits

      def applyRefundableCredits(credits: RefundableTaxCredit*): TaxOutcome =
        val netCredit = credits.combineAll

        self match
          case Left(taxPayable) =>
            if taxPayable.size <= netCredit.size then
              (netCredit reduceByTaxPayable taxPayable).asTaxRefundable.asRight
            else (taxPayable reduceByRefundableCredit netCredit).asLeft

          case Right(taxRefundable) =>
            (taxRefundable + netCredit.asTaxRefundable).asRight
        end match
      end applyRefundableCredits

      private def asSignedDouble: Double = self match
        case Left(tp) =>
          // Positive: tax is payable.
          tp.asDouble

        case Right(tr) =>
          // Negative: tax is refunded
          -tr.asDouble
    end extension

    given Monoid[TaxOutcome] = Monoid.instance(
      emptyValue = ofPayable(TaxPayable(0)),
      cmb = (x, y) =>
        val dbl = x.asSignedDouble + y.asSignedDouble
        if dbl < 0.0
        then ofRefundable(TaxRefundable(-dbl))
        else ofPayable(TaxPayable(dbl))
    )
  end TaxOutcome

end TaxOutcomes
