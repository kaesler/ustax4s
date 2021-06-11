package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.*

case class Schedule4(
  selfEmploymentTax: TMoney
):
  def totalOtherTaxes: TMoney = selfEmploymentTax
