package org.kae.withdrawals

import breeze.linalg.{DenseVector, norm}

object DemoApp extends App {
  import breeze.optimize.linear._
  val lp = new LinearProgram()
  import lp._
  val x0 = Real()
  val x1 = Real()
  val x2 = Real()

  val lpp =  ( (x0 +  x1 * 2 + x2 * 3 )
    subjectTo ( x0 * -1 + x1 + x2 <= 20)
    subjectTo ( x0 - x1 * 3 + x2 <= 30)
    subjectTo ( x0 <= 40 )
    )

  val result = maximize( lpp)

  assert( norm(result.result - DenseVector(40.0,17.5,42.5), 2) < 1E-4)

  println("good")
}
