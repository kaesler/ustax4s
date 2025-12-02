package org.kae.ustax4s

import org.kae.ustax4s.money.Moneys.IncomeThreshold

type Bracket[T] = (threshold: IncomeThreshold, rate: T)
