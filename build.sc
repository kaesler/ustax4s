import mill._
import mill.scalalib._
import scalafmt._

object ustax4s extends SbtModule with ScalafmtModule {
  def scalaVersion = "3.2.2"
  
  def scalacOptions = Seq(
      "-deprecation",
      "-explain-types",
      "-feature",
      "-source:future",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xmigration"
    )  

  def ivyDeps = Agg( 
    ivy"org.typelevel::cats-core:2.9.0",
    ivy"org.typelevel::cats-effect:3.4.8"
  )
  object test extends Tests with TestModule.Munit {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit:0.7.29",
      ivy"org.scalameta::munit-scalacheck:0.7.29"
    )
  }
}
