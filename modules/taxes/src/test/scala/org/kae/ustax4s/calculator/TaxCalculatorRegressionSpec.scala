package org.kae.ustax4s.calculator

import munit.FunSuite
import org.kae.ustax4s.calculator.testdata.RegressionTestCase

class TaxCalculatorRegressionSpec extends FunSuite {
  test("Regression tests pass") {
    RegressionTestCase.all.foreach(_.run)
  }
}
