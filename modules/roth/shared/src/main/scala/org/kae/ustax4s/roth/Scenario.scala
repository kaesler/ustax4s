package org.kae.ustax4s.roth

import java.time.Year
import org.kae.ustax4s.money.Moneys.IncomeThreshold
import org.kae.ustax4s.money.Moneys.Income

trait Scenario:
  def year: Year
  def fillableOrdinaryIncomeFedThresholds: Vector[IncomeThreshold]
  def fillableOrdinaryIncomeStateThresholds: Vector[IncomeThreshold]

  def rothConversionAmount: Income
  def taxRateOnRothConversion: Double

  def fill(threshold: IncomeThreshold): Scenario
end Scenario
