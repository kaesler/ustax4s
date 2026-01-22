package org.kae.ustax4s.federal.yearly

import java.time.Year

object DisplayBracketChangesAdhocTest:
  def main(args: Array[String]): Unit =
    val years = (2017 to 2022).toList
      .map(Year.of)
      .flatMap(YearlyValues.averageThresholdChangeOverPrevious)
    println(years)
  end main
end DisplayBracketChangesAdhocTest
