package org.kae.ustax4s.kevin

import java.time.{LocalDate, Year}
import org.kae.ustax4s.FilingStatus

object Kevin:
  import FilingStatus.*

  val birthDate: LocalDate = LocalDate.of(1955, 10, 2)

  def filingStatus(year: Year): FilingStatus =
    if year.getValue <= 2027 then HeadOfHousehold else Single

  def personalExemptions(year: Year): Int =
    if year.getValue <= 2027 then 2 else 1

  def numberOfMassachusettsDependents(year: Year): Int =
    if year.getValue <= 2027 then 1 else 0
