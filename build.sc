import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._
import scalafmt._

trait CommonSettings extends ScalaModule {
  override def scalaVersion: T[String] = T {"3.2.2"}
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

  val testDeps = List(
    ivy"org.scalameta::munit:0.7.29",
    ivy"org.scalameta::munit-scalacheck:0.7.29"
  )
}

object ustax4s extends Module  {

  object js extends ScalaJSModule with CommonSettings {
    override def scalaJSVersion = "1.13.0"
    override def millSourcePath = super.millSourcePath / os.up

    override def ivyDeps = Agg(
      ivy"org.typelevel::cats-core:2.9.0",
      ivy"org.typelevel::cats-effect:3.4.8"
    )
    object test extends Tests with TestModule.Munit {
      override def ivyDeps = Agg(testDeps:_*)
    }
  }

  object jvm extends ScalaModule with CommonSettings with ScalafmtModule {
    def debug = T { millSourcePath.toString }

    // Note: default path is ustax4s/jvm
    // This is expected to contain a "src" sub dir.
    override def millSourcePath = super.millSourcePath / os.up

    object test extends Tests with TestModule.Munit {
      override def ivyDeps = Agg(testDeps:_*)
    }
  }
}
