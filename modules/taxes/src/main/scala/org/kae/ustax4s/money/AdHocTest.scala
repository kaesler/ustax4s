package org.kae.ustax4s.money

@main def main() =
  import MoneySyntax.*
  val im: TMoney = 10
  val dm: TMoney = 100.00
  println(dm + im - 20)
  println(109.678.asMoney)
