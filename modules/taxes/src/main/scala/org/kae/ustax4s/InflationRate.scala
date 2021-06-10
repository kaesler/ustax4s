package org.kae.ustax4s

import eu.timepit.refined.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval

/** Cost-of-living based growth rates.
  */
type InflationRate = Double Refined Interval.Closed[0.0d, 0.20d]
