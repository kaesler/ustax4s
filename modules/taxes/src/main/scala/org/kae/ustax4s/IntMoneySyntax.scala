package org.kae.ustax4s

trait IntMoneySyntax:
  // TODO: Use Scala3
  implicit class IntOps(i: Int):
    def tm: TMoney = TMoney.u(i)

object IntMoneySyntax extends IntMoneySyntax
