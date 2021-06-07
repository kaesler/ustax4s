package org.kae.ustax4s
package testdata

final case class RegressionTestCase(
  filingStatus: FilingStatus,
  dependents: Int,
  socSec: TMoney,
  ordinaryIncomeNonSS: TMoney,
  qualifiedIncome: TMoney,
  federalTaxDue: TMoney,
  stateTaxDue: TMoney
)
