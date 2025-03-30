package org.kae

import sbt._

object Dependencies {

  object Cats {
    val core: ModuleID   = "org.typelevel" %% "cats-core"   % "2.13.0"
    val effect: ModuleID = "org.typelevel" %% "cats-effect" % "3.6.0"
  }

  object MUnit {
    val munit: ModuleID      = "org.scalameta" %% "munit"            % "1.1.0"
    val scalacheck: ModuleID = "org.scalameta" %% "munit-scalacheck" % "1.1.0"
  }

  object Scalacheck {
    val scalacheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.15.4"
  }
}
