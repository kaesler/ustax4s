package org.kae.ustax4s.money

import cats.Monoid
import cats.syntax.all.*
import scala.annotation.targetName

// Type for the ultimate outcome for State or Federal, or both.
export TaxOutcomes.TaxOutcome
object TaxOutcomes:
  import org.kae.ustax4s.money.NonNegMoneys.{TaxPayable, TaxRefundable}

  opaque type TaxOutcome = Either[TaxPayable, TaxRefundable]

  object TaxOutcome:
    @targetName("ofTaxPayable")
    def of(tp: TaxPayable): TaxOutcome = tp.asLeft
    @targetName("ofTaxRefundable")
    def of(tr: TaxRefundable): TaxOutcome = tr.asRight

    extension (to: TaxOutcome)
      def +(other: TaxOutcome): TaxOutcome =
        summon[Monoid[TaxOutcome]].combine(to, other)

      def asSignedDouble: Double = to match
        case Left(tp) =>
          // Positive: tax is payable.
          tp.asDouble

        case Right(tr) =>
          // Negative: tax is refunded
          -tr.asDouble
    end extension

    given Monoid[TaxOutcome] = Monoid.instance(
      emptyValue = of(TaxPayable(0)),
      cmb = (x, y) =>
        val dbl = x.asSignedDouble + y.asSignedDouble
        if dbl < 0.0
        then of(TaxRefundable(-dbl))
        else of(TaxPayable(dbl))
    )
  end TaxOutcome

end TaxOutcomes
