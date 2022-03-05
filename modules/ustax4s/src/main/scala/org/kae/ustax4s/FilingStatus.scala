package org.kae.ustax4s

import cats.Show

enum FilingStatus(val isSingle: Boolean):
  case Single          extends FilingStatus(true)
  case HeadOfHousehold extends FilingStatus(true)
  case Married         extends FilingStatus(false)

object FilingStatus:
  given Show[FilingStatus] with
    def show(fs: FilingStatus): String = fs.productPrefix
end FilingStatus
