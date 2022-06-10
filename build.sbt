import microsites._

ThisBuild / tlBaseVersion := "1.2"
ThisBuild / startYear := Some(2021)
ThisBuild / homepage := Some(url("https://typelevel.org/cats-mtl/"))
ThisBuild / developers := List(
  tlGitHubDev("SystemFw", "Fabio Labella"),
  tlGitHubDev("andyscott", "Andy Scott"),
  tlGitHubDev("kailuowang", "Kailuo Wang"),
  tlGitHubDev("djspiewak", "Daniel Spiewak"),
  tlGitHubDev("LukaJCB", "Luka Jacobowitz"),
  tlGitHubDev("edmundnoble", "Edmund Noble")
)

val Scala213 = "2.13.8"

ThisBuild / crossScalaVersions := Seq("3.1.1", "2.12.16", Scala213)
ThisBuild / tlVersionIntroduced := Map("3" -> "1.2.1")

ThisBuild / githubWorkflowBuildPreamble ++= Seq(
  WorkflowStep.Use(
    UseRef.Public("ruby", "setup-ruby", "v1"),
    name = Some("Setup Ruby"),
    params = Map("ruby-version" -> "2.7")),
  WorkflowStep.Run(List("gem install jekyll -v 4.0.0"), name = Some("Install Jekyll"))
)

ThisBuild / githubWorkflowBuild +=
  WorkflowStep.Sbt(
    List("makeMicrosite"),
    name = Some("Make microsite"),
    cond = Some(s"matrix.scala == '$Scala213'"))

ThisBuild / githubWorkflowPublishPostamble += WorkflowStep.Sbt(List("publishMicrosite"))

ThisBuild / testFrameworks += new TestFramework("munit.Framework")

lazy val commonJvmSettings = Seq(
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
)

lazy val commonJsSettings = Seq(
  doctestGenTests := Seq.empty
)

val CatsVersion = "2.7.0"

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, lawsJVM, lawsJS, docs, testsJVM, testsJS)
  .settings(name := "root")
  .enablePlugins(NoPublishPlugin)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(name := "cats-mtl")
  .settings(
    libraryDependencies += "org.typelevel" %%% "cats-core" % CatsVersion,
    Compile / packageSrc / mappings ++= {
      val base = (Compile / sourceManaged).value
      (Compile / managedSources).value.map(file => file -> file.relativeTo(base).get.getPath)
    }
  )
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val docsMappingsAPIDir =
  settingKey[String]("Name of subdirectory in site target directory for api docs")

lazy val docs = project
  .enablePlugins(MicrositesPlugin, ScalaUnidocPlugin, MdocPlugin, NoPublishPlugin)
  .settings(name := "cats-mtl-docs")
  .settings(
    crossScalaVersions := (ThisBuild / crossScalaVersions).value.filter(_.startsWith("2.")),
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
      file("CONTRIBUTING.md") -> ExtraMdFileConfig(
        "contributing.md",
        "home",
        Map("title" -> "Contributing", "section" -> "contributing", "position" -> "50")),
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
    mdocIn := (Compile / sourceDirectory).value / "mdoc",
    autoAPIMappings := true,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(coreJVM, lawsJVM),
    docsMappingsAPIDir := "api",
    addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, docsMappingsAPIDir),
    scalacOptions := scalacOptions.value.filterNot(_ == "-Werror"),
    ghpagesNoJekyll := false,
    tlFatalWarningsInCi := false,
    ScalaUnidoc / unidoc / scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-doc-source-url",
      scmInfo.value.get.browseUrl + "/tree/main€{FILE_PATH}.scala",
      "-sourcepath",
      (LocalRootProject / baseDirectory).value.getAbsolutePath,
      "-diagrams"
    ),
    git.remoteRepo := "git@github.com:typelevel/cats-mtl.git",
    makeSite / includeFilter := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
  )
  .settings(commonJvmSettings)
  .dependsOn(coreJVM)

lazy val laws = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core)
  .settings(name := "cats-mtl-laws")
  .settings(libraryDependencies += "org.typelevel" %%% "cats-laws" % CatsVersion)
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)

lazy val lawsJVM = laws.jvm
lazy val lawsJS = laws.js

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core, laws)
  .enablePlugins(NoPublishPlugin)
  .settings(name := "cats-mtl-tests")
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-testkit" % CatsVersion,
    "org.typelevel" %%% "discipline-munit" % "1.0.9"))
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)
  .jsSettings(Test / scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)))

lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js
