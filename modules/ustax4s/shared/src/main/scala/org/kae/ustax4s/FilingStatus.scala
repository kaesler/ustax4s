package org.kae.ustax4s

import cats.Show
import org.kae.ustax4s.states.MaritalStatus
import org.kae.ustax4s.states.MaritalStatus.{Married, Unmarried}

enum FilingStatus(val headCount: Int):
  case Single          extends FilingStatus(1)
  case HeadOfHousehold extends FilingStatus(1)
  case MarriedJoint    extends FilingStatus(2)

  def maritalStatus: MaritalStatus =
    if headCount == 1 then Unmarried else Married
  def isSingle: Boolean = headCount == 1
end FilingStatus

object FilingStatus:

  given Show[FilingStatus]:
    def show(fs: FilingStatus): String = fs.productPrefix

  given Ordering[FilingStatus]:
    def compare(x: FilingStatus, y: FilingStatus): Int =
      x.ordinal.compare(y.ordinal)

end FilingStatus
