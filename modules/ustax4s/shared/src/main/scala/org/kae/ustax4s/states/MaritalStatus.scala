package org.kae.ustax4s.states

import org.kae.ustax4s.FilingStatus

enum MaritalStatus:
  case Married
  case Unmarried

  def of(filingStatus: FilingStatus): MaritalStatus =
    if filingStatus.isSingle then Unmarried else Married
end MaritalStatus


