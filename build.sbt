import microsites._
import sbtcrossproject.{crossProject, CrossType}

// project info

organization in ThisBuild := "org.typelevel"

// aliases

addCommandAlias("buildJVM", "testsJVM/test")

addCommandAlias(
  "validateJVM",
  ";scalastyle;sbt:scalafmt::test;scalafmt::test;test:scalafmt::test;buildJVM;mimaReportBinaryIssues;makeMicrosite")

addCommandAlias("validateJS", ";testsJS/compile;testsJS/test")

addCommandAlias("validate", ";clean;validateJS;validateJVM")

addCommandAlias("gitSnapshots",
                ";set version in ThisBuild := git.gitDescribedVersion.value.get + \"-SNAPSHOT\"")

sbtPlugin := true

publishMavenStyle := false

// projects

val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(moduleName := "cats-mtl-core", name := "Cats MTL core")
  .settings(Settings.coreSettings: _*)
  .settings(Settings.includeGeneratedSrc)
  .settings(Dependencies.acyclic)
  .settings(Dependencies.catsCore)
  .settings(Dependencies.scalaCheck)
  .jsSettings(Settings.commonJsSettings: _*)
  .jvmSettings(Settings.commonJvmSettings: _*)

val coreJVM = core.jvm
val coreJS = core.js

lazy val docsMappingsAPIDir =
  settingKey[String]("Name of subdirectory in site target directory for api docs")

lazy val docSettings = Seq(
  micrositeName := "Cats MTL",
  micrositeDescription := "Monad Transformers made easy",
  micrositeAuthor := "Typelevel contributors",
  micrositeHighlightTheme := "atom-one-light",
  //  micrositeHomepage := "http://typelevel.org/cats",
  micrositeBaseUrl := "cats-mtl",
  micrositeDocumentationUrl := "api",
  micrositeGithubOwner := "typelevel",
  micrositeDocumentationLabelDescription := "Scaladoc",
  micrositeExtraMdFiles := Map(
    file("CONTRIBUTING.md") -> ExtraMdFileConfig("contributing.md",
                                                 "home",
                                                 Map("title" -> "Contributing",
                                                     "section" -> "contributing",
                                                     "position" -> "50")),
    file("README.md") -> ExtraMdFileConfig(
      "index.md",
      "home",
      Map("title" -> "Home", "section" -> "home", "position" -> "0")
    )
  ),
  micrositeGithubRepo := "cats-mtl",
  micrositePalette := Map(
    "brand-primary" -> "#7B7998",
    "brand-secondary" -> "#393E63",
    "brand-tertiary" -> "#323759",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"
  ),
  autoAPIMappings := true,
  unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(coreJVM, lawsJVM),
  docsMappingsAPIDir := "api",
  addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), docsMappingsAPIDir),
  ghpagesNoJekyll := false,
  fork in tut := true,
  fork in (ScalaUnidoc, unidoc) := true,
  scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
    "-Xfatal-warnings",
    "-doc-source-url",
    scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
    "-sourcepath",
    baseDirectory.in(LocalRootProject).value.getAbsolutePath,
    "-diagrams"
  ),
  git.remoteRepo := "git@github.com:typelevel/cats-mtl.git",
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
)

lazy val crossVersionSharedSources: Seq[Setting[_]] =
  Seq(Compile, Test).map { sc =>
    (unmanagedSourceDirectories in sc) ++= {
      (unmanagedSourceDirectories in sc).value.map { dir: File =>
        new File(dir.getPath + "_" + scalaBinaryVersion.value)
      }
    }
  }

val docs = project
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(ScalaUnidocPlugin)
  .settings(moduleName := "cats-mtl-docs")
  .settings(Settings.coreSettings)
  .settings(Publishing.noPublishSettings)
  .settings(docSettings)
  .settings(Settings.commonJvmSettings)
  .dependsOn(coreJVM)

val laws = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core)
  .settings(moduleName := "cats-mtl-laws", name := "Cats MTL laws")
  .settings(Settings.coreSettings: _*)
  .settings(Dependencies.catsBundle: _*)
  .settings(Dependencies.catalystsAndScalatest: _*)
  .jsSettings(Settings.commonJsSettings: _*)
  .jvmSettings(Settings.commonJvmSettings: _*)
  .jsSettings(coverageEnabled := false)

val lawsJVM = laws.jvm
val lawsJS = laws.js

val tests = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core, laws)
  .settings(moduleName := "cats-mtl-tests")
  .settings(Settings.coreSettings: _*)
  .settings(Dependencies.catsBundle: _*)
  .settings(Dependencies.scalaCheck: _*)
  .settings(Publishing.noPublishSettings: _*)
  .settings(Dependencies.catalystsAndScalatest: _*)
  .jsSettings(Settings.commonJsSettings: _*)
  .jvmSettings(Settings.commonJvmSettings: _*)

val testsJVM = tests.jvm
val testsJS = tests.js

val catsMtl = project
  .in(file("."))
  .settings(moduleName := "root")
  .settings(Settings.coreSettings)
  .settings(Publishing.noPublishSettings)
  .aggregate(coreJVM, coreJS, lawsJVM, lawsJS, docs, testsJVM, testsJS)
