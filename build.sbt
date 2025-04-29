import org.kae.Dependencies.*

ThisBuild / organization := "org.kae"
ThisBuild / version      := "1.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.4"

lazy val root = project
  .in(file("."))
  .aggregate(ustax4s.jvm, ustax4s.js)
  .dependsOn(ustax4s.jvm, ustax4s.js)
  .settings(
    name           := "root",
    publish / skip := true
  )

lazy val gsheetfacade = (project in file("modules/gsheetfacade"))
  .dependsOn(ustax4s.js)
  .settings(
    name := "gsheetfacade",
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
    )
  )
lazy val ustax4s = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/ustax4s"))
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
