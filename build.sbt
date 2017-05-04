import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import ReleaseTransformations._
import sbtunidoc.ScalaUnidocPlugin.autoImport._
import scala.xml.transform.{RewriteRule, RuleTransformer}
import org.scalajs.sbtplugin.cross.CrossProject

// CI

lazy val botBuild = settingKey[Boolean]("Build by TravisCI instead of local dev environment")

// coverage

lazy val scoverageSettings = Seq(
  coverageMinimum := 90,
  coverageFailOnMinimum := false
)

def disableScoverage210Js(crossProject: CrossProject) =
  crossProject
  .jsSettings(
    coverageEnabled := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) => false
        case _ => coverageEnabled.value
      }
    }
  )

def disableScoverage210Js: Project ⇒ Project = p =>
  p.settings(
    coverageEnabled := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) => false
        case _ => coverageEnabled.value
      }
    }
  )

def disableScoverage210Jvm(crossProject: CrossProject) =
  crossProject
  .jvmSettings(
    coverageEnabled := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) => false
        case _ => coverageEnabled.value
      }
    }
  )

// project info

organization in ThisBuild := "org.typelevel"

// scalac settings

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        Seq()
      case Some((2, n)) if n >= 11 =>
        Seq("-Ywarn-unused-import")
    }
  },
  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)

lazy val update2_12 = Seq(
  scalacOptions -= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 12)) => "-Yinline-warnings"
      case _ => ""
    }
  }
)

val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

// docs

def docsSourcesAndProjects(sv: String): (Boolean, Seq[ProjectReference]) =
  CrossVersion.partialVersion(sv) match {
    case Some((2, 10)) => (false, Nil)
    case _ => (true, Seq()) // kernelJVM, coreJVM, freeJVM))
  }

lazy val docsMappingsAPIDir = settingKey[String]("Name of subdirectory in site target directory for api docs")

lazy val javadocSettings = Seq(
  sources in (Compile, doc) := (if (docsSourcesAndProjects(scalaVersion.value)._1) (sources in (Compile, doc)).value else Nil)
)

lazy val docSettings = Seq(
  micrositeName := "Cats MTL",
  micrositeDescription := "Companion library to cats providing monad transformers",
  micrositeAuthor := "Typelevel contributors",
  micrositeHighlightTheme := "atom-one-light",
//  micrositeHomepage := "http://typelevel.org/cats",
  micrositeBaseUrl := "cats-mtl",
  micrositeDocumentationUrl := "api",
  micrositeGithubOwner := "edmundnoble",
  micrositeExtraMdFiles := Map(file("CONTRIBUTING.md") -> "contributing.md"),
  micrositeGithubRepo := "cats-mtl",
  micrositePalette := Map(
    "brand-primary" -> "#5B5988",
    "brand-secondary" -> "#292E53",
    "brand-tertiary" -> "#222749",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"),
  autoAPIMappings := true,
  unidocProjectFilter in (ScalaUnidoc, unidoc) :=
    inProjects(docsSourcesAndProjects(scalaVersion.value)._2:_*),
  docsMappingsAPIDir := "api",
  addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), docsMappingsAPIDir),
  ghpagesNoJekyll := false,
  fork in tut := true,
  fork in (ScalaUnidoc, unidoc) := true,
  scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
    "-Xfatal-warnings",
    "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
    "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
    "-diagrams"
  ),
  git.remoteRepo := "git@github.com:edmundnoble/cats-mtl.git",
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
)

lazy val crossVersionSharedSources: Seq[Setting[_]] =
  Seq(Compile, Test).map { sc =>
    (unmanagedSourceDirectories in sc) ++= {
      (unmanagedSourceDirectories in sc ).value.map {
        dir:File => new File(dir.getPath + "_" + scalaBinaryVersion.value)
      }
    }
  }

val saneSettings = Seq(
  fork in test := true

)

// aliases

addCommandAlias("buildJVM", "catsJVM/test")

addCommandAlias("validateJVM", ";scalastyle;buildJVM;mimaReportBinaryIssues;makeMicrosite")

addCommandAlias("validateJS", ";catsJS/compile;testsJS/test;js/test")

addCommandAlias("validateKernelJS", "kernelLawsJS/test")

addCommandAlias("validateFreeJS", "freeJS/test") //separated due to memory constraint on travis

addCommandAlias("validate", ";clean;validateJS;validateKernelJS;validateFreeJS;validateJVM")


