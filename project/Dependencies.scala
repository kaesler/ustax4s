package org.kae

import sbt._

object Dependencies {

  object Cats {
    val core: ModuleID =
      "org.typelevel" %% "cats-core" % "2.13.0" exclude ("org.scala-lang", "scala3-library_3")
    val effect: ModuleID =
      "org.typelevel" %% "cats-effect" % "3.6.1" exclude ("org.scala-lang", "scala3-library_3")
  }

  object MUnit {
    val munit: ModuleID =
      "org.scalameta" %% "munit" % "1.2.1" exclude ("org.scala-lang", "scala3-library_3")
    val scalacheck: ModuleID =
      "org.scalameta" %% "munit-scalacheck" % "1.2.0" exclude ("org.scala-lang", "scala3-library_3")
  }

  object Scalacheck {
    val scalacheck: ModuleID = "org.scalacheck" %% "scalacheck" % "1.15.4"
  }

  object ScalajsTime {
    val time: ModuleID = "io.github.cquiroz" %% "scala-java-time" % "2.6.0"
  }

}
