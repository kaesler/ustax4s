package org.kae.ustax4s

import cats.Show

enum FilingStatus(val entryName: String, val isSingle: Boolean):
  case Single          extends FilingStatus("Single", true)
  case HeadOfHousehold extends FilingStatus("HeadOfHousehold", true)

object FilingStatus:
  given Show[FilingStatus] with
    def show(fs: FilingStatus) = fs.entryName
end FilingStatus
