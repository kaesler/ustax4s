package org.kae.ustax4s
package federal.forms

case class Schedule4(
  selfEmploymentTax: TMoney
):
  def totalOtherTaxes: TMoney = selfEmploymentTax
