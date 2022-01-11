import org.kae.Dependencies._

ThisBuild / organization := "org.kae"
ThisBuild / version      := "1.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.1.0"

lazy val root = project
  .in(file("."))
  .aggregate(ustax4s)
  .dependsOn(ustax4s)
  .settings(
    name           := "root",
    publish / skip := true
  )

lazy val ustax4s = (project in file("modules/ustax4s"))
  .settings(
    name := "ustax4s",
    libraryDependencies ++= Seq(
      Cats.core withSources (),
      Cats.effect withSources (),
      MUnit.munit      % Test withSources (),
      MUnit.scalacheck % Test withSources ()
    ),
    scalacOptions := Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings",
      "-unchecked",
      "-language:implicitConversions",
      "-source:future"
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    Test / parallelExecution := false
  )
