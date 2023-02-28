import mill._
import mill.scalalib._
import scalafmt._

object ustax4s extends Module  {

  def scalaJSVersion = "1.13.0"

  object jvm extends ScalaModule with ScalafmtModule {
    def debug = T { millSourcePath.toString }
    override def scalaVersion = "3.2.2"

    // Note: default path is ustax4s/jvm
    // This is expected to contain a "src" sub dir.

     override def millSourcePath = super.millSourcePath / os.up

     override def scalacOptions = Seq(
      "-deprecation",
      "-explain-types",
      "-feature",
      "-source:future",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xmigration"
    )

    override def ivyDeps = Agg(
      ivy"org.typelevel::cats-core:2.9.0",
      ivy"org.typelevel::cats-effect:3.4.8"
    )
    object test extends Tests with TestModule.Munit {
      override def ivyDeps = Agg(
        ivy"org.scalameta::munit:0.7.29",
        ivy"org.scalameta::munit-scalacheck:0.7.29"
      )
    }
  }
}
