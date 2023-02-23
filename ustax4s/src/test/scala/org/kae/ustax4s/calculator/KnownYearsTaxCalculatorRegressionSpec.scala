package org.kae.ustax4s.calculator

import munit.FunSuite
import org.kae.ustax4s.calculator.testdata.knownyears.KnownYearRegressionTestCase

class KnownYearsTaxCalculatorRegressionSpec extends FunSuite:
  test("Known year regression tests pass") {
    KnownYearRegressionTestCase.all.foreach(_.run())
  }
