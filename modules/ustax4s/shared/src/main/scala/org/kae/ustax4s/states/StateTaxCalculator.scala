package org.kae.ustax4s.states

import org.kae.ustax4s.Brackets.Brackets
import org.kae.ustax4s.federal.{FederalCalcInput, FederalCalcResults}
import org.kae.ustax4s.money.Moneys.TaxPayable

// Model this: https://docs.google.com/spreadsheets/d/1I_OuA6uuAs7YoZRc02atHCCxpXaGDn9N/edit?gid=201796956#gid=201796956

sealed trait StateTaxCalculator:
  // TODO: Should this be a "bind" operation to produce a bound calculator
  def stateTaxDue(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): TaxPayable
  
end StateTaxCalculator

object StateTaxCalculator:
  import State.*
  def of(state: State): StateTaxCalculator =
    state match
      case AK | FL | NV | NH | SD | TN | TX | WA | WY => NilStateTaxCalculator

      case CO =>
        // From fed taxable income.
        FlatStateTaxCalculator(StateTaxRate.unsafeFrom(4.4 / 100))
      case IL => FlatStateTaxCalculator(StateTaxRate.unsafeFrom(4.95 / 100))
      case IN => FlatStateTaxCalculator(StateTaxRate.unsafeFrom(3.05 / 100))
      case MI => FlatStateTaxCalculator(StateTaxRate.unsafeFrom(4.25 / 100))
      case NC => FlatStateTaxCalculator(StateTaxRate.unsafeFrom(4.25 / 100))
      case PA => FlatStateTaxCalculator(StateTaxRate.unsafeFrom(3.07 / 100))
      case UT => FlatStateTaxCalculator(StateTaxRate.unsafeFrom(4.85 / 100))

      // Progressive: TODO
      case AL => ???
      case AZ => ???
      case AR => ???
      case AS => ???
      case CA => ???
      case CT => ???
      case DE => ???
      case DC => ???
      case GA => ???
      case GU => ???
      case HI => ???
      case ID => ???
      case IA => ???
      case KS => ???
      case KY => ???
      case LA => ???
      case ME => ???
      case MD => ???
      case MA => ???
      case MN => ???
      case MS => ???
      case MO => ???
      case MT => ???
      case NE => ???
      case NJ => ???
      case NM => ???
      case NY => ???
      case ND => ???
      case MP => ???
      case OH => ???
      case OK => ???
      case OR => ???
      case PR => ???
      case RI => ???
      case SC => ???
      case TT => ???
      case VT => ???
      case VA => ???
      case VI => ???
      case WV => ???
      case WI => ???
    end match
  end of
end StateTaxCalculator

case object NilStateTaxCalculator extends StateTaxCalculator:

  override def stateTaxDue(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): TaxPayable = TaxPayable.zero
end NilStateTaxCalculator

case class FlatStateTaxCalculator(
  rate: StateTaxRate
) extends StateTaxCalculator:

  override def stateTaxDue(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): TaxPayable =
    ???
end FlatStateTaxCalculator

case class ProgressiveStateTaxCalculator(
  brackets: Brackets[StateTaxRate]
) extends StateTaxCalculator:

  override def stateTaxDue(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): TaxPayable =
    ???
end ProgressiveStateTaxCalculator
