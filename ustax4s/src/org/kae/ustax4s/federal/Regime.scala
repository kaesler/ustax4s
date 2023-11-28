package org.kae.ustax4s.federal

import cats.Show
import scala.util.Try

export Regime.TCJA
export Regime.PreTCJA

enum Regime:
  case TCJA    extends Regime
  case PreTCJA extends Regime
end Regime

object Regime:
  def parse(s: String): Option[Regime] = Try(Regime.valueOf(s)).toOption
  def unsafeParse(s: String): Regime = parse(s).getOrElse(
    throw Exception(s"Invalid Regime name: %s")
  )

  given Show[Regime] with
    def show(r: Regime): String = r.productPrefix
end Regime
