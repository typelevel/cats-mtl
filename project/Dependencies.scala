import sbt.{Def, _}
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Dependencies {

  //noinspection TypeAnnotation
  object Versions {
    val scalaCheck = "1.13.5"
    val scalaTest = "3.0.5"
    val discipline = "0.9.0"
    val macroParadise = "2.1.1"
    val kindProjector = "0.9.8"
    val simulacrum = "0.13.0"
    val machinist = "0.6.5"
    val cats = "1.4.0"
    val shapeless = "2.3.3"
  }

  val acyclic: Seq[Def.Setting[_]] = Def.settings(
    libraryDependencies += "com.lihaoyi" %% "acyclic" % "0.1.8" % "provided",
    autoCompilerPlugins := true,
    addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.8"),
    scalacOptions += "-P:acyclic:force"
  )

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
    compilerPlugin("org.scalamacros" %% "paradise" % Versions.macroParadise cross CrossVersion.patch),
    compilerPlugin("org.spire-math" %% "kind-projector" % Versions.kindProjector)
  ))

  val catsBundle: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core" % Versions.cats,
    "org.typelevel" %%% "cats-free" % Versions.cats,
    "org.typelevel" %%% "cats-laws" % Versions.cats
  ))

  val catsCore: Seq[Setting[_]] = Def.settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core" % Versions.cats
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
