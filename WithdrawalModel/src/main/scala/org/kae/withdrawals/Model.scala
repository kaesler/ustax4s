package org.kae.withdrawals

object Model {
  // Parameters
  //  - initial balances
  //    - taxable
  //    - roth
  //    - taxDeferred
  //  - inflation rate (assumed constant)
  //  - taxFilingStatus: year => {Single, HOH}
  //  - Federal tax
  //    - brackets (adjusted yearly by some constant rate)
  //    - standard deduction
  //    - standard exemption (adjusted yearly by some constant rate)
  //  - State tax
  //    - rate assumed constant
  //    - standard exemption (adjusted yearly by some constant rate)
  //  - special expenditures (year)
  //  - dividend assumptions
  //  - capital gain assumptions
  //  - horizon
  //  - Social Security income beginning 70 (adjusted yearly by some constant rate)
  //  - Federal Tax on Social Security
  //  - Medicare Premiums
  //
  // Objective:
  //  - maximize present value of all future net incomes
  //
  // Variables:
  //  - taxableWithdrawal(year)
  //  - ltcgProportion(year)
  //  - rothConversion(year)
  //  - iraWithdrawal(year)
  //  - rothWithdrawal(year)
  //  - income(year, taxBracketRate)
  //
  // Constraints:
  //  - account withdrawal in year t must be <= total available at end
  //    of year t - 1.
  //  - sum of taxable income by tax bracket = total taxable income
  //  - income in a given bracket is <= size of the bracket
  //  - taxDeferred withdrawals >= RMDs from 72
  //  - Federal taxes due
  //    - capital gains
  //    - dividends
  //    - ira withdrawals
  //    - social security
  //  - State taxes due
  //    - capital gains
  //    - dividends
  //    - ira withdrawals
  //  - finish with zero
  //  - minimum disposable income (living expenses)


  // Simple initial model
  //  - 1 IRA
  //  - 10 years
  //  - HOH
  //  - verify the tax results against my tax library
  //  - no Social security
  //
  //  Then add:
  //  - 30 years
  //  - tax filing status changes
  //  - taxable account
  //  - etc

  // We generate the model from iterative Scala code
}
