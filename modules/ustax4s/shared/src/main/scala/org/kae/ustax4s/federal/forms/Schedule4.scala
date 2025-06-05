package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.TaxPayable

case class Schedule4(
  selfEmploymentTax: TaxPayable
):
  def totalOtherTaxes: TaxPayable = selfEmploymentTax
end Schedule4
