import microsites._
import sbtcrossproject.{crossProject, CrossType}

replaceCommandAlias("ci", "; project /; headerCheck; scalafmtCheckAll; clean; test; mimaReportBinaryIssues; makeMicrosite")
replaceCommandAlias("release", "; reload; project /; +mimaReportBinaryIssues; +publish; sonatypeBundleRelease; publishMicrosite")

ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"

ThisBuild / baseVersion := "1.0"

ThisBuild / developers := List(
  Developer(
    "SystemFw",
    "Fabio Labella",
    "",
    url("https://github.com/SystemFw/")),
  Developer(
    "andyscott",
    "Andy Scott",
    "",
    url("https://github.com/andyscott/")),
  Developer(
    "kailuowang",
    "Kailuo Wang",
    "",
    url("https://github.com/kailuowang/")),
  Developer(
    "djspiewak",
    "Daniel Spiewak",
    "",
    url("https://github.com/djspiewak/")),
  Developer(
    "LukaJCB",
    "Luka Jacobowitz",
    "",
    url("https://github.com/LukaJCB/")),
  Developer(
    "edmundnoble",
    "Edmund Noble",
    "",
    url("https://github.com/edmundnoble/")))

ThisBuild / scalaVersion := crossScalaVersions.value.last
ThisBuild / crossScalaVersions := Seq("2.12.11", "2.13.2")

ThisBuild / githubWorkflowBuildPreamble ++= Seq(
  WorkflowStep.Use(
    "actions", "setup-ruby", "v1",
    name = Some("Setup Ruby"),
    params = Map("ruby-version" -> "2.7")),

  WorkflowStep.Run(
    List("gem install jekyll -v 4.0.0"),
    name = Some("Install Jekyll")))

ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("ci")))

ThisBuild / githubWorkflowPublishTargetBranches := Seq()

lazy val commonJvmSettings = Seq(
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"))

lazy val commonJsSettings = Seq(
  scalacOptions += {
    val a = (LocalRootProject / baseDirectory).value.toURI.toString
    val g = s"https://raw.githubusercontent.com/typelevel/cats/v${version.value}"
    s"-P:scalajs:mapSourceURI:$a->$g/"
  },

  doctestGenTests := Seq.empty)

val CatsVersion = "2.2.0"

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, lawsJVM, lawsJS, docs, testsJVM, testsJS)
  .settings(name := "root")
  .settings(noPublishSettings)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(name := "cats-mtl")
  .settings(
    libraryDependencies += "org.typelevel" %%% "cats-core" % CatsVersion,

    Compile / packageSrc / mappings ++= {
      val base = (Compile / sourceManaged).value
      (Compile / managedSources).value.map { file =>
        file -> file.relativeTo(base).get.getPath
      }
    })
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val docsMappingsAPIDir =
  settingKey[String]("Name of subdirectory in site target directory for api docs")

lazy val docs = project
  .enablePlugins(MicrositesPlugin, ScalaUnidocPlugin, MdocPlugin)
  .settings(name := "cats-mtl-docs")
  .settings(noPublishSettings)
  .settings(
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
    micrositeExtraMdFilesOutput := resourceManaged.value / "main" / "jekyll",
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
    micrositeCompilingDocsTool := WithMdoc,
    mdocIn := (Compile / sourceDirectory).value / "mdoc",
    autoAPIMappings := true,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(coreJVM, lawsJVM),
    docsMappingsAPIDir := "api",
    addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, docsMappingsAPIDir),
    scalacOptions := scalacOptions.value.filterNot(_ == "-Werror"),
    ghpagesNoJekyll := false,
    mdoc / fork := true,
    fatalWarningsInCI := false,
    ScalaUnidoc / unidoc / fork := true,
    ScalaUnidoc / unidoc / scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-doc-source-url",
      scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
      "-sourcepath",
      (LocalRootProject / baseDirectory).value.getAbsolutePath,
      "-diagrams"
    ),
    git.remoteRepo := "git@github.com:typelevel/cats-mtl.git",
    makeSite / includeFilter := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md")
  .settings(commonJvmSettings)
  .dependsOn(coreJVM)

lazy val laws = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core)
  .settings(name := "cats-mtl-laws")
  .settings(libraryDependencies += "org.typelevel" %%% "cats-laws" % CatsVersion )
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)

lazy val lawsJVM = laws.jvm
lazy val lawsJS = laws.js

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core, laws)
  .settings(name := "cats-mtl-tests")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-testkit"         % CatsVersion,
      "org.typelevel" %%% "discipline-scalatest" % "2.0.1"))
  .settings(noPublishSettings)
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)

lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js
