package org.kae.ustax4s

enum FilingStatus(val entryName: String):
  case Single extends FilingStatus("Single")
  case HeadOfHousehold extends FilingStatus("HeadOfHousehold")
