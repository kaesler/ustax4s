import org.kae.Dependencies.*

ThisBuild / organization := "org.kae"
ThisBuild / version      := "1.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.4.2"

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
      "-Wnonunit-statement",
      "-Wunused:explicits",
      "-Wunused:implicits",
      "-Wunused:imports",
      "-Wunused:locals",
      "-Wunused:params",
      "-Wunused:privates",
      "-Wvalue-discard",
      "-Xfatal-warnings",
      "-Xmigration",
      "-deprecation",
      "-explain-types",
      "-feature",
      "-language:implicitConversions",
      "-source:future",
      "-unchecked"
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    Test / parallelExecution := false
  )
