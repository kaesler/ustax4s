package org.kae.ustax4s.federal

import java.time.Year

final case class NotYetImplemented(year: Year)
    extends RuntimeException(s"No implementation for ${year.getValue}")
