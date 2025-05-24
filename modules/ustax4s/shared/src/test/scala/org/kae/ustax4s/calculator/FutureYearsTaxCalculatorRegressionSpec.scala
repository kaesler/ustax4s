package org.kae.ustax4s.calculator

import munit.FunSuite
import org.kae.ustax4s.calculator.testdata.futureyears.FutureYearRegressionTestCase

class FutureYearsTaxCalculatorRegressionSpec extends FunSuite:
  test("Future year regression tests pass") {
    FutureYearRegressionTestCase.all.foreach(_.run())
  }
