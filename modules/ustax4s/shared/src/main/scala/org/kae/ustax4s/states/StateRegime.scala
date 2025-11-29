package org.kae.ustax4s.states

import org.kae.ustax4s.Brackets.Brackets

sealed trait StateRegime

case object NilStateRegime extends StateRegime

case class FlatStateRegime(
  rate: StateTaxRate
) extends StateRegime

case class ProgressiveStateRegime(
  brackets: Brackets[StateTaxRate]
) extends StateRegime

object StateRegime:
  import State.*
  def of(state: State): StateRegime =
    state match
      case AK | FL | NV | NH | SD | TN | TX | WA | WY => NilStateRegime

      case CO =>
        // From fed taxable income.
        FlatStateRegime(StateTaxRate.unsafeFrom(4.4 / 100))
      case IL => FlatStateRegime(StateTaxRate.unsafeFrom(4.95 / 100))
      case IN => FlatStateRegime(StateTaxRate.unsafeFrom(3.05 / 100))
      case MI => FlatStateRegime(StateTaxRate.unsafeFrom(4.25 / 100))
      case NC => FlatStateRegime(StateTaxRate.unsafeFrom(4.25 / 100))
      case PA => FlatStateRegime(StateTaxRate.unsafeFrom(3.07 / 100))
      case UT => FlatStateRegime(StateTaxRate.unsafeFrom(4.85 / 100))

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
end StateRegime
