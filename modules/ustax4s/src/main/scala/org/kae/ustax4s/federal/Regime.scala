package org.kae.ustax4s.federal

import cats.Show
import scala.util.Try

export Regime.Trump
export Regime.PreTrump

enum Regime:
  case Trump    extends Regime
  case PreTrump extends Regime
end Regime

object Regime:
  def parse(s: String): Option[Regime] = Try(Regime.valueOf(s)).toOption
  def unsafeParse(s: String): Regime   = valueOf(s)

  given Show[Regime] with
    def show(r: Regime): String = r.productPrefix
end Regime
