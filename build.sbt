import org.kae.Dependencies._

lazy val retirement = project
  .in(file("."))
  .aggregate(taxes)
  .dependsOn(taxes)
  .settings(
    name         := "retirement",
    version      := "1.0",
    scalaVersion := "3.1.0"
  )

lazy val taxes = (project in file("modules/taxes"))
  .settings(
    name         := "taxes",
    scalaVersion := "3.0.0",
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
