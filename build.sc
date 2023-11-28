import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._
import scalafmt._

trait CommonSettings extends ScalaModule {
  override def scalaVersion: T[String] = T {"3.3.1"}
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
    ivy"org.typelevel::cats-core:2.10.0",
    ivy"org.typelevel::cats-effect:3.5.2",
    ivy"io.github.cquiroz::scala-java-time:2.5.0"
  )

  val testDeps = List(
    ivy"org.scalameta::munit:0.7.29",
    ivy"org.scalameta::munit-scalacheck:0.7.29"
  )
}

object ustax4s extends Module  {

  object jsfacade extends ScalaJSModule with CommonSettings {
    override def moduleDeps = List(ustax4s.js)
    override def scalaJSVersion = "1.14.0"
  }
  
  object js extends ScalaJSModule with CommonSettings {
    override def scalaJSVersion = "1.14.0"
    override def millSourcePath = super.millSourcePath / os.up

    override def ivyDeps = Agg(
      ivy"org.typelevel::cats-core:2.10.0",
      ivy"org.typelevel::cats-effect:3.5.2"
    )
    object test extends ScalaJSTests with TestModule.Munit {
      override def ivyDeps = Agg(testDeps:_*)
    }
  }

  object jvm extends ScalaModule with CommonSettings with ScalafmtModule {
    def debug = T { millSourcePath.toString }

    // Note: default path is ustax4s/jvm
    // This is expected to contain a "src" sub dir.
    override def millSourcePath = super.millSourcePath / os.up

    object test extends ScalaTests with TestModule.Munit {
      override def ivyDeps = Agg(testDeps:_*)
    }
  }
}
