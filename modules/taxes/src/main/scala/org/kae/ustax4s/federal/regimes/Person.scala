package org.kae.ustax4s.federal.regimes

import org.kae.ustax4s.FilingStatus
import java.time.LocalDate

final case class Person(
  birthDate: LocalDate,
  personalExemptions: Int
)