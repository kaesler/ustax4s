import org.kae.Dependencies._

ThisBuild / scalaVersion := "2.13.5"

lazy val retirement = project
  .in(file("."))
  .aggregate(taxes)
  .dependsOn(taxes)
  .settings(
    name := "retirement",
    version := "0.1"
  )

lazy val taxes = (project in file("modules/taxes"))
  .settings(
    name := "taxes",
    libraryDependencies ++= Seq(
      Cats.core,
      Cats.effect,
      Enumeratum.enumeratum,
      Refined.refined,
      Specs2.core       % Test,
      Specs2.scalacheck % Test
    ),
    scalacOptions := Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings",
      "-unchecked",
      "-Xcheckinit",
      "-Ywarn-dead-code",
      "-Ywarn-extra-implicit",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused:implicits",
      "-Ywarn-unused:imports",
      "-Ywarn-unused:locals",
      "-Ywarn-unused:params",
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:privates",
      "-Ywarn-value-discard"
    )
  )

lazy val withdrawals = (project in file("modules/withdrawals"))
  .settings(
    name := "withdrawals",
    libraryDependencies ++= Seq(
      Scalanlp.breeze,
      Scalanlp.natives
    )
  )
