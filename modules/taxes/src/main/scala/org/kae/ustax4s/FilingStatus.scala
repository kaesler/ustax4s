package org.kae.ustax4s

enum FilingStatus(val entryName: String, val isSingle: Boolean):
  case Single          extends FilingStatus("Single", true)
  case HeadOfHousehold extends FilingStatus("HeadOfHousehold", true)
