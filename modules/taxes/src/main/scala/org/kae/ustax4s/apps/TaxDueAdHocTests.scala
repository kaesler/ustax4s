package org.kae.ustax4s.apps

import java.time.Year
import org.kae.ustax4s.FilingStatus.Single
import org.kae.ustax4s.{IntMoneySyntax, SimpleTaxInRetirement}

object TaxDueAdHocTests extends App with IntMoneySyntax{
  val res = SimpleTaxInRetirement.taxDue(
    Year.of(2029),
    Single,
    17335.tm,
    49128.tm
  )
  println(res)
}
