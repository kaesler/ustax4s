
name := "ustax4s"

version := "0.1"

scalaVersion := "2.13.0"

//scalacOptions += "-Ypartial-unification"

libraryDependencies ++= Seq(
  "com.beachape" %% "enumeratum" % "1.5.13",
  "org.typelevel" %% "cats-core" % "2.0.0-M4",
  "org.typelevel" %% "cats-effect" % "2.0.0-M4",
  "eu.timepit" %% "refined" % "0.9.8",
  "co.fs2" %% "fs2-core" % "1.1.0-M1",
  "co.fs2" %% "fs2-io" % "1.1.0-M1",
  "org.scalacheck" %% "scalacheck" % "1.14.0" % "test",
  "org.specs2" %% "specs2-core" % "4.6.0" % "test",
  "org.specs2" %% "specs2-scalacheck" % "4.6.0" % "test"



)
