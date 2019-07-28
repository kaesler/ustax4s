package org.kae.ustax4s

import org.kae.tagged.Tag.{@@, Tagged}

object TaggedTypes {

  trait StandardDeductionTag

  type StandardDeduction = TMoney @@ StandardDeductionTag

  object StandardDeduction extends Tagged[TMoney, StandardDeductionTag] {
    def of(status: FilingStatus): StandardDeduction = ???
    // We'll want to compute it for a given year
  }
}
