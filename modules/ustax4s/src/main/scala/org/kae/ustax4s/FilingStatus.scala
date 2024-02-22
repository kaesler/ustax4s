package org.kae.ustax4s

import cats.Show

enum FilingStatus(val isSingle: Boolean):
  case Single          extends FilingStatus(true)
  case HeadOfHousehold extends FilingStatus(true)
  case MarriedJoint    extends FilingStatus(false)

object FilingStatus:
  given Show[FilingStatus] with
    def show(fs: FilingStatus): String = fs.productPrefix

  given Ordering[FilingStatus] with
    def compare(x: FilingStatus, y: FilingStatus): Int =
      x.ordinal.compare(y.ordinal)

end FilingStatus
