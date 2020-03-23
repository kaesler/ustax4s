package org.kae.ustax4s

import org.scalacheck.Gen

trait TMoneyGeneration {
  val genMoney: Gen[TMoney] = for {
    dollars <- Gen.choose(0, 5000000)
  } yield TMoney.u(dollars)
}
