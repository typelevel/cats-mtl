import sbt.{Def, _}
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Dependencies {

  //noinspection TypeAnnotation
  object Versions {
    val scalaCheck = "1.13.4"
    val scalaTest = "3.0.0"
    val discipline = "0.7.2"
    val macroParadise = "2.1.0"
    val kindProjector = "0.9.3"
    val simulacrum = "0.10.0"
    val machinist = "0.6.1"
    val cats = "0.9.0"
    val shapeless = "2.3.2"
  }

  val discipline: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "discipline" % Versions.discipline
  ))

  val scalaCheck: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % Versions.scalaCheck
  ))

  val catalystsAndScalatest: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "catalysts-platform" % "0.0.5",
    "org.typelevel" %%% "catalysts-macros" % "0.0.5" % "test",
    "org.scalatest" %%% "scalatest" % Versions.scalaTest % "test"
  ))

  val compilerPlugins: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    compilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.patch),
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")
  ))

  val catsCore: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core" % Versions.cats
  ))

  val catsLaws: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-laws" % Versions.cats
  ))

  val shapeless: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "com.chuusai" %%% "shapeless" % Versions.shapeless
  ))

  val simulacrumAndMachinist: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "com.github.mpilquist" %%% "simulacrum" % Versions.simulacrum,
    "org.typelevel" %%% "machinist" % Versions.machinist
  ))

  val sonatypeResolvers: Seq[Setting[_]] = Def.settings(resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ))

  val bintrayResolvers: Seq[Setting[_]] = Def.settings(
    resolvers += "bintray/non" at "http://dl.bintray.com/non/maven"
  )

}