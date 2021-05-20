package org.kae.ustax4s.apps

import java.time.Year

import org.kae.ustax4s.IntMoneySyntax
import org.kae.ustax4s.Kevin
import org.kae.ustax4s.FilingStatus
import org.kae.ustax4s.inretirement.TaxInRetirement

object TaxDueAdHocTests extends App with IntMoneySyntax {

  TaxInRetirement.federalTaxDue(
    year = Year.of(2021),
    Kevin.birthDate,
    FilingStatus.Single,
    socSec = 0.tm,
    ordinaryIncomeNonSS = 0.tm,
    qualifiedIncome = 50000.tm
  )
}
