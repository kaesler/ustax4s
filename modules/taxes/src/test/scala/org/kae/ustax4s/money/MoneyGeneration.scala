package org.kae.ustax4s.money

import org.scalacheck.Gen

trait MoneyGeneration:
  val genMoney: Gen[Money] =
    for dollars <- Gen.choose(0, 5000000)
    yield dollars
