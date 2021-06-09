package org.kae.ustax4s

trait IntMoneySyntax:
  // TODO: Use Scala3
  implicit class IntOps(i: Int):
    def asMoney: TMoney = TMoney(i)

object IntMoneySyntax extends IntMoneySyntax
