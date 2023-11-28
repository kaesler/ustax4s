package org.kae.ustax4s

import cats.Show
import scala.util.Try

enum FilingStatus(val isSingle: Boolean):
  case Single          extends FilingStatus(true)
  case HeadOfHousehold extends FilingStatus(true)
  case Married         extends FilingStatus(false)

object FilingStatus:
  def parse(s: String): Option[FilingStatus] = Try(FilingStatus.valueOf(s)).toOption
  def unsafeParse(s: String): FilingStatus = parse(s).getOrElse(
    throw Exception(s"Invalid FilingStatus name: %s")
  )

  given Show[FilingStatus] with
    def show(fs: FilingStatus): String = fs.productPrefix

  given Ordering[FilingStatus] with
    def compare(x: FilingStatus, y: FilingStatus): Int =
      x.ordinal.compare(y.ordinal)

end FilingStatus
