package org.kae.ustax4s.federal.forms

import org.kae.ustax4s.money.*

case class Schedule4(
  selfEmploymentTax: Money
):
  def totalOtherTaxes: Money = selfEmploymentTax
