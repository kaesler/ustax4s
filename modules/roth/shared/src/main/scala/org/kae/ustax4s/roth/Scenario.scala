package org.kae.ustax4s.roth

import org.kae.ustax4s.money.{Income, IncomeThreshold}

import java.time.Year

trait Scenario:
  def year: Year
  def fillableOrdinaryIncomeFedThresholds: Vector[IncomeThreshold]
  def fillableOrdinaryIncomeStateThresholds: Vector[IncomeThreshold]

  def rothConversionAmount: Income
  def taxRateOnRothConversion: Double

  def fill(threshold: IncomeThreshold): Scenario
end Scenario
