package org.kae.ustax4s.moneys

import cats.Show
import cats.implicits.*
import org.kae.ustax4s.money.Money

opaque type IncomeThreshold = Money

// TODO: These are always integers.

object IncomeThreshold:

  given Show[IncomeThreshold] = summonShow

  extension (underlying: IncomeThreshold)
    def subtractFrom(money: Money): Money =
      money subp underlying

end IncomeThreshold
private def summonShow = summon[Show[Money]]
