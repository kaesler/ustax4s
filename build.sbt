import org.kae.Dependencies._

// ThisBuild / scalaVersion := "3.0.0"

lazy val retirement = project
  .in(file("."))
  .aggregate(taxes)
  .dependsOn(taxes)
  .settings(
    name         := "retirement",
    version      := "1.0",
    scalaVersion := "3.0.2"
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
      "-language:implicitConversions"
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    Test / parallelExecution := false
  )

lazy val withdrawals = (project in file("modules/withdrawals"))
  .settings(
    name := "withdrawals",
    libraryDependencies ++= Seq(
      Scalanlp.breeze,
      Scalanlp.natives
    )
  )
