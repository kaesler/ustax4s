package org.kae.ustax4s

import munit.FunSuite
import org.kae.ustax4s.testdata.RegressionTestCase

class RegressionSpec extends FunSuite {
  test("Regression tests pass") {
    RegressionTestCase.all.foreach(_.run)
  }
}
