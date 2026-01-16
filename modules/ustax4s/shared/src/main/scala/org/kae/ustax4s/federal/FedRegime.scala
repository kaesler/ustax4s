package org.kae.ustax4s.federal

import cats.Show
import scala.util.Try

export FedRegime.TCJA
export FedRegime.PreTCJA

enum FedRegime:
  case TCJA    extends FedRegime
  case PreTCJA extends FedRegime
end FedRegime

object FedRegime:
  def parse(s: String): Option[FedRegime] = Try(FedRegime.valueOf(s)).toOption
  def unsafeParse(s: String): FedRegime   = valueOf(s)

  given Show[FedRegime]:
    def show(r: FedRegime): String = r.productPrefix
end FedRegime
