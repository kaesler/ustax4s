package org.kae.tagged

import org.kae.tagged.Tag.@@

trait TaggedSyntax {

  implicit class TaggedOps[A, T](v: A @@ T) {
    def untag: A = v
  }

}
object TaggedSyntax extends TaggedSyntax
