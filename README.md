# Federal and State(MA) tax calculator and predictor

Simplified to cover many common scenarios for retirees.

Ignored:
  - standard deduction adjustment for blindness
  - various phaseouts for high incomes

## Build for GSHeet
In my repo called Scala-Google-Spreadsheets:
 - publishLocal

In this repo (for SBT 1.x)
  - sbt
    - project gsheetfacade
    - fullCompileCreateFunctions
  - This writes a google.js at /gsheetfacade/target/scalaXX/google.js

