package org.kae.ustax4s

import scala.collection.immutable

sealed trait FilingStatus extends Product {
  def entryName: String = productPrefix
}

object FilingStatus {
  case object Single          extends FilingStatus
  case object HeadOfHousehold extends FilingStatus

  def values: immutable.IndexedSeq[FilingStatus] = IndexedSeq(Single, HeadOfHousehold)
}
