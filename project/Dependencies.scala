package org.kae

import sbt._

object Dependencies {

  object Cats {
    val core: ModuleID   = "org.typelevel" %% "cats-core"   % "2.9.0"
    val effect: ModuleID = "org.typelevel" %% "cats-effect" % "3.4.8"
  }

  object MUnit {
    val munit: ModuleID      = "org.scalameta" %% "munit"            % "0.7.29"
    val scalacheck: ModuleID = "org.scalameta" %% "munit-scalacheck" % "0.7.29"
  }

  object Scalacheck {
    val scalacheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.15.4"
  }
}
