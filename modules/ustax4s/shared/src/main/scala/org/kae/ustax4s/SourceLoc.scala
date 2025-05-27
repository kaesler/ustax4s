package org.kae.ustax4s

import scala.quoted.*

object SourceLoc:
  // https://eed3si9n.com/intro-to-scala-3-macros/

  inline def apply(): String = ${ sourceLocation() }
  private def sourceLocation()(
    using Quotes
  ): Expr[String] =
    import quotes.reflect.*
    val pos      = Position.ofMacroExpansion
    val line     = pos.startLine + 1
    val fileName = pos.sourceFile.name
    Expr(s"$fileName: $line")
end SourceLoc
