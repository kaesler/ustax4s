import org.kae.Dependencies.*
import org.scalajs.linker.interface.ESVersion
import sbt.Keys.libraryDependencies
import scala.collection.Seq

ThisBuild / organization := "org.kae"
ThisBuild / version      := "1.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.2"

val fastCompileRenderer = taskKey[File]("Return main file")

lazy val fastCompileCreateFunctions =
  taskKey[Unit]("Fast compile, and adds to the compiled file the created functions")

val fullCompileRenderer = taskKey[File]("Return full optimized main file")

lazy val fullCompileCreateFunctions =
  taskKey[Unit]("Full compile, and adds to the compiled file the created functions")

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
  .aggregate(gsheetfacade, ustax4sJS, ustax4sJVM, roth.js, roth.jvm)
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
    name          := "gsheetfacade",
    scalacOptions := commonScalacOptions ++ Seq(
      "-scalajs"
    ),
    publish / skip := true,
    scalaJSLinkerConfig ~= {
      _.withESFeatures(
        _.withESVersion(
          // Note: Anything later results in linked output
          // which uses the "||=" operator, which fails to
          // parse in the Google AppScript (V8) interpreter.
          ESVersion.ES2020
        )
      )
    },
    fastCompileRenderer := {
      (Compile / fastOptJS).value.data
    },
    fullCompileRenderer := {
      (Compile / fullOptJS).value.data
    },
    fastCompileCreateFunctions := {
      GSheetFunctions.createGoogleFunctions(
        fastCompileRenderer.value,
        baseDirectory.value
      )
    },
    fullCompileCreateFunctions := {
      GSheetFunctions.createGoogleFunctions(
        fullCompileRenderer.value,
        baseDirectory.value
      )
    },
    libraryDependencies ++=
      Seq(
        "org.kae" %%% "cells" % "0.1-SNAPSHOT"
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

lazy val roth = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .dependsOn(ustax4s)
  .in(file("modules/roth"))
  .settings(
    name := "roth",
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

lazy val ustax4sJS  = ustax4s.js
lazy val ustax4sJVM = ustax4s.jvm
