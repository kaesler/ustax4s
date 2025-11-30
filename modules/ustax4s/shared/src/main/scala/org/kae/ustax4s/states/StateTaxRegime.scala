package org.kae.ustax4s.states

import org.kae.ustax4s.Brackets.Brackets
import org.kae.ustax4s.federal.{FederalCalcInput, FederalCalcResults}
import org.kae.ustax4s.money.Moneys.TaxPayable
import scala.annotation.unused

// Model this: https://docs.google.com/spreadsheets/d/1I_OuA6uuAs7YoZRc02atHCCxpXaGDn9N/edit?gid=201796956#gid=201796956

sealed trait HasStateTaxDue:
  def stateTaxDue: TaxPayable
end HasStateTaxDue

sealed trait StateTaxRegime

object StateTaxRegime:
  import State.*
  def of(state: State): StateTaxRegime =
    state match
      case AK | FL | NV | NH | SD | TN | TX | WA | WY => NilStateTaxRegime

      case CO =>
        // From fed taxable income.
        FlatStateTaxRegime(StateTaxRate.unsafeFrom(4.4 / 100))
      case IL => FlatStateTaxRegime(StateTaxRate.unsafeFrom(4.95 / 100))
      case IN => FlatStateTaxRegime(StateTaxRate.unsafeFrom(3.05 / 100))
      case MI => FlatStateTaxRegime(StateTaxRate.unsafeFrom(4.25 / 100))
      case NC => FlatStateTaxRegime(StateTaxRate.unsafeFrom(4.25 / 100))
      case PA => FlatStateTaxRegime(StateTaxRate.unsafeFrom(3.07 / 100))
      case UT => FlatStateTaxRegime(StateTaxRate.unsafeFrom(4.85 / 100))

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
end StateTaxRegime

sealed trait HasStateTaxCalculator:
  def calculator(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): HasStateTaxDue

case object NilStateTaxRegime extends StateTaxRegime, HasStateTaxDue:
  override val stateTaxDue: TaxPayable = TaxPayable.zero
end NilStateTaxRegime

class FlatStateTaxRegime(
  @unused
  rate: StateTaxRate
) extends StateTaxRegime,
      HasStateTaxCalculator:
  override def calculator(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): HasStateTaxDue = ???
end FlatStateTaxRegime

class ProgressiveStateTaxRegime(
  @unused
  brackets: Brackets[StateTaxRate]
) extends StateTaxRegime,
      HasStateTaxCalculator:
  override def calculator(
    federalInput: FederalCalcInput,
    federalCalcResults: FederalCalcResults
  ): HasStateTaxDue = ???
end ProgressiveStateTaxRegime
