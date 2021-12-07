package org.kae.ustax4s.moneys

import cats.Semigroup

import cats.implicits.*
import org.kae.ustax4s.money.Money

opaque type IncomeThreshold = Money

// TODO: These are always integers.

object IncomeThreshold:

  extension (underlying: IncomeThreshold)
    def subtractFrom(money: Money): Money =
      money subp underlying

end IncomeThreshold
