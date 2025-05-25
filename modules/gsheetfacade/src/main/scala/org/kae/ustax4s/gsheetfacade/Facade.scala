package org.kae.ustax4s.gsheetfacade

import org.kae.ustax4s.federal.BoundRegime
import scala.scalajs.js.annotation.JSExportTopLevel

object Facade:
  import TypeAliases.*
  import Conversions.given


 /**
  * Standard deduction for a known year.
  * Example: TIR_STD_DEDUCTION(2022, "HeadOfHousehold", 1955-10-02)
  *
  * @param {number} year the tax regime to use, one of "TCJA", "PreTCJA"
  * @param {string} filingStatus one of "Single", "HeadOfHousehold", "Married"
  * @param {object} birthDate tax payer's date of birth
  * @returns {number} The standard deduction
  * @customfunction
  */
  @JSExportTopLevel("tir_std_deduction")
  def tir_std_deduction(
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
  end tir_std_deduction

end Facade
