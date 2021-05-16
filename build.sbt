import org.kae.Dependencies._

name := "retirement"

version := "0.1"

ThisBuild / scalaVersion := "2.13.5"

lazy val taxes = (project in file("modules/taxes"))
  .settings(
    name := "taxes",
    libraryDependencies ++= Seq(
      Cats.core,
      Cats.effect,
      Enumeratum.enumeratum,
      Refined.refined,
      Specs2.core % Test,
      Specs2.scalacheck % Test
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
