package org.kae

import sbt._

object Dependencies {

  object Cats {
    val core: ModuleID = "org.typelevel" %% "cats-core" % "2.1.1"
    val effect: ModuleID = "org.typelevel" %% "cats-effect" % "2.1.2"
  }

  object Enumeratum {
    val enumeratum: ModuleID = "com.beachape" %% "enumeratum" % "1.5.14"
  }

  object Fs2 {
    val core: ModuleID = "co.fs2" %% "fs2-core" % "2.3.0"
    val io: ModuleID = "co.fs2" %% "fs2-io" % "2.3.0"
  }

  object Refined {
    val refined: ModuleID = "eu.timepit" %% "refined" % "0.9.13"
  }

  object Scalacheck {
    val scalacheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.14.1"
  }

  object Specs2 {
    val core: ModuleID = "org.specs2" %% "specs2-core" % "4.8.3"
    val scalacheck: ModuleID = "org.specs2" %% "specs2-scalacheck" % "4.8.3"
  }
}

