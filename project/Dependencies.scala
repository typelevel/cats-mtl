import sbt._, Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Dependencies {

  lazy val scalaCheckVersion = "1.13.4"
  lazy val scalaTestVersion = "3.0.0"
  lazy val disciplineVersion = "0.7.2"

  lazy val disciplineDependencies = Seq(
    libraryDependencies += "org.scalacheck" %%% "scalacheck" % scalaCheckVersion,
    libraryDependencies += "org.typelevel" %%% "discipline" % disciplineVersion
  )

  lazy val testingDependencies = Seq(
    libraryDependencies += "org.typelevel" %%% "catalysts-platform" % "0.0.5",
    libraryDependencies += "org.typelevel" %%% "catalysts-macros" % "0.0.5" % "test",
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalaTestVersion % "test"
  )

}