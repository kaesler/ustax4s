package org.kae.ustax4s

import enumeratum.EnumEntry
import enumeratum.Enum
import scala.collection.immutable

sealed trait FilingStatus extends EnumEntry

object FilingStatus extends Enum[FilingStatus] {
  case object Single extends FilingStatus
  case object HeadOfHousehold extends FilingStatus

  override def values: immutable.IndexedSeq[FilingStatus] = findValues
}

