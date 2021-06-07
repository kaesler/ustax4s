package org.kae.ustax4s.money

@main def main() =
  import MoneySyntax.*
  val im: Money = 10
  val dm: Money = 100.00
  println(dm + im - 20)
  println(109.678.asMoney)
