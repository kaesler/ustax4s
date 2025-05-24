package org.kae.ustax4s.gsheetfacade

import org.kae.ustax4s.federal.BoundRegime
import scala.scalajs.js.annotation.JSExportTopLevel

object Facade:
  import TypeAliases.*
  import Conversions.given

  @JSExportTopLevel("stdDeduction")
  def stdDeduction(
    year: GYear,
    filingStatus: GFilingStatus,
    birthDate: GLocalDate
  ): GDeduction =
    // TODO:
    //  - Use implicit conversion givens
    //  - Obtain a BoundRegime
    //  - get result
    //  - convert result
    val regime = BoundRegime.forKnownYear(year, filingStatus)
    regime.standardDeduction(birthDate)
  end stdDeduction

end Facade
