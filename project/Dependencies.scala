package org.kae

import sbt._

object Dependencies {

  object Cats {
    val core: ModuleID   = "org.typelevel" %% "cats-core"   % "2.7.0"
    val effect: ModuleID = "org.typelevel" %% "cats-effect" % "3.3.1"
  }

  object MUnit {
    val munit: ModuleID      = "org.scalameta" %% "munit"            % "0.7.29"
    val scalacheck: ModuleID = "org.scalameta" %% "munit-scalacheck" % "0.7.29"
  }

  object Scalacheck {
    val scalacheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.15.4"
  }

  object Scalanlp {
    val breeze: ModuleID  = "org.scalanlp" %% "breeze"         % "1.0"
    val natives: ModuleID = "org.scalanlp" %% "breeze-natives" % "1.0"
    val viz: ModuleID     = "org.scalanlp" %% "breeze-viz"     % "1.0"
  }
}
