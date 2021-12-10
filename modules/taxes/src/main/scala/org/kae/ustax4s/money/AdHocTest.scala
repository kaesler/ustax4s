package org.kae.ustax4s.money

@main def main(): Unit =
  val im: Money = 10
  val dm: Money = 100.00
  println(dm + im subp 20)
  println(Money(109.678))
