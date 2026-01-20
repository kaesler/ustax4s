package org.kae.ustax4s.federal

import munit.FunSuite
import org.kae.ustax4s.federal.testdata.knownyears.KnownYearRegressionTestCase

class KnownYearsFedCalculatorRegressionSpec extends FunSuite:
  test("Known year regression tests pass") {
    KnownYearRegressionTestCase.all.foreach(_.run())
  }
