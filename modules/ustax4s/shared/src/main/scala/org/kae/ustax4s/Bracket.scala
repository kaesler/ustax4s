package org.kae.ustax4s

import org.kae.ustax4s.money.IncomeThreshold

type Bracket[T] = (threshold: IncomeThreshold, rate: T)
