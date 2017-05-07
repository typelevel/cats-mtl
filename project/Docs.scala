import sbt.{Def, _}
import Keys._
import microsites.MicrositeKeys._
import sbtunidoc.ScalaUnidocPlugin.autoImport._
import sbtunidoc.BaseUnidocPlugin.autoImport._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.GitPlugin.autoImport._
import com.typesafe.sbt.site.SitePlugin.autoImport._
import com.github.tkawachi.doctest.DoctestPlugin.autoImport._
import tut.Plugin._

object Docs {
  def docsSourcesAndProjects(sv: String): (Boolean, Seq[ProjectReference]) = {
    CrossVersion.partialVersion(sv) match {
      case Some((2, 10)) => (false, Nil)
      case _ => (true, Seq()) // kernelJVM, coreJVM, freeJVM))
    }
  }

  lazy val docsMappingsAPIDir = settingKey[String]("Name of subdirectory in site target directory for api docs")

  lazy val javadocSettings = Seq(
    sources in(Compile, doc) := {
      if (docsSourcesAndProjects(scalaVersion.value)._1) (sources in(Compile, doc)).value
      else Nil
    }
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
    unidocProjectFilter in(ScalaUnidoc, unidoc) :=
      inProjects(docsSourcesAndProjects(scalaVersion.value)._2: _*),
    docsMappingsAPIDir := "api",
    addMappingsToSiteDir(mappings in(ScalaUnidoc, packageDoc), docsMappingsAPIDir),
    ghpagesNoJekyll := false,
    fork in tut := true,
    fork in(ScalaUnidoc, unidoc) := true,
    scalacOptions in(ScalaUnidoc, unidoc) ++= Seq(
      "-Xfatal-warnings",
      "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
      "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
      "-diagrams"
    ),
    git.remoteRepo := "git@github.com:edmundnoble/cats-mtl.git",
    includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
  )

  val catsDoctestSettings: Seq[Setting[_]] = Def.settings(
    doctestWithDependencies := false
  )

  lazy val crossVersionSharedSources: Seq[Setting[_]] =
    Seq(Compile, Test).map { sc =>
      (unmanagedSourceDirectories in sc) ++= {
        (unmanagedSourceDirectories in sc).value.map {
          dir: File => new File(dir.getPath + "_" + scalaBinaryVersion.value)
        }
      }
    }

}
