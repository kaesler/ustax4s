package org.kae.ustax4s.money

import org.scalacheck.Gen

trait MoneyGeneration:
  val genDollars: Gen[Int]   = Gen.choose(0, 5000000)
  val genIncome: Gen[Income] = genDollars.map(Income.apply)
