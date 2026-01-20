package org.kae.ustax4s.federal

import munit.FunSuite
import org.kae.ustax4s.federal.testdata.futureyears.FutureYearRegressionTestCase

class FutureYearsFedCalculatorRegressionSpec extends FunSuite:
  test("Future year regression tests pass") {
    FutureYearRegressionTestCase.all.foreach(_.run())
  }
