package org.kae.ustax4s

// TODO: would be nice to use NonEmptyMap but cats prevents us from creating
// one outside cats.
case class TaxBrackets(bracketStarts: Map[TMoney, TaxRate]) {
  require (bracketStarts.contains(TMoney.zero))

  val bracketStartsAscending: Vector[(TMoney, TaxRate)] = bracketStarts.toVector.sortBy(_._1)

  private val bracketsStartsDescending = bracketStartsAscending.reverse

  def taxDue(m: TMoney): TMoney = {
    val pair = bracketsStartsDescending.foldLeft(m, TMoney.zero) {

      case ((yetToBeTaxed, taxSoFar), (bracketStart, bracketRate)) =>

        // Note: because subtract does not give a a value below zero
        // both the following values will be zero if the bracket does not
        // apply.
        val amountInThisBracket = yetToBeTaxed subtract bracketStart
        val taxInThisBracket = amountInThisBracket * bracketRate
        (yetToBeTaxed subtract amountInThisBracket, taxSoFar + taxInThisBracket)
    }
    assert(pair._1.isZero)
    pair._2
  }

  def isProgressive: Boolean = {
    val rates = bracketStartsAscending.map(_._2)
    (rates zip rates.tail)
      .forall { case (left, right) =>
        left < right
      }
  }
}
