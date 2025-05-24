import org.kae.Dependencies.*

ThisBuild / organization := "org.kae"
ThisBuild / version      := "1.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.0"

lazy val commonScalacOptions = Seq(
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

lazy val root = project
  .in(file("."))
  .aggregate(gsheetfacade, ustax4sJS, ustax4sJVM)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name           := "root",
    publish / skip := true
  )

lazy val gsheetfacade = project
  .in(file("modules/gsheetfacade"))
  .dependsOn(ustax4sJS)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "gsheetfacade",
    scalacOptions := commonScalacOptions ++ Seq(
      "-scalajs"
    )
  )

lazy val ustax4s = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("modules/ustax4s"))
  .settings(
    name := "ustax4s",
    libraryDependencies ++= {
      def triplePercent(m: ModuleID): ModuleID = {
        import m.*
        organization %%% name % revision
      }

      Seq(
        triplePercent(Cats.core withSources ()),
        triplePercent(MUnit.munit      % Test withSources ()),
        triplePercent(MUnit.scalacheck % Test withSources ()),
        triplePercent(ScalajsTime.time)
      )
    },
    excludeDependencies ++= Seq(
      ExclusionRule("scala-lang.org")
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    Test / parallelExecution := false
  )
  .jvmSettings(
    scalacOptions := commonScalacOptions
  )
  .jsSettings(
    scalacOptions := commonScalacOptions ++ Seq("-scalajs")
  )

lazy val ustax4sJS = ustax4s.js
lazy val ustax4sJVM = ustax4s.jvm

