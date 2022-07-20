package org.kae.ustax4s.calculator

import munit.FunSuite
import org.kae.ustax4s.calculator.testdata.knownyears.KnownYearRegressionTestCase

class TaxCalculatorRegressionSpec extends FunSuite:
  test("Regression tests pass") {
    KnownYearRegressionTestCase.all.foreach(_.run())
  }

