package org.kae.ustax4s.inretirement

import munit.FunSuite
import org.kae.ustax4s.inretirement.testdata.RegressionTestCase

class RegressionSpec extends FunSuite {
  test("Regression tests pass") {
    RegressionTestCase.all.foreach(_.run)
  }
}