package org.kae.withdrawals

object Model {
  // Parameters
  //  - initial balances
  //  - inflation
  //  - tax brackets (year)
  //  - tax brackets inflation
  //  - special expenditures (year)
  //  - dividend assumptions
  //  - capital gain assumptions
  //  - horizon
  //  - Social Security income beginning 70
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
  //  - RMDs from 72
  //  - taxes due (Federal and state)
  //    - capital gains
  //    - dividends
  //    - ira withdrawals
  //    - social security
  //  - finish with zero
  //  - minimum disposable income (living expenses)


  // Simple initial model
  //  - 1 IRA
  //  - 10 years
  //  - HOH
  //  - verify the tax results against my tax library
  //
  //  Then add:
  //  - 30 years
  //  - tax filing status changes
  //  - taxable account
}
