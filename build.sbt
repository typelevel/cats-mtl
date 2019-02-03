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

publishMavenStyle := false

// common settings

lazy val includeGeneratedSrc: Seq[Setting[_]] = Seq(
  mappings in (Compile, packageSrc) ++= {
    val base = (sourceManaged in Compile).value
    (managedSources in Compile).value.map { file =>
      file -> file.relativeTo(base).get.getPath
    }
  }
)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  incOptions := incOptions.value.withLogRecompileOnMacro(false),
  scalacOptions ++= CompilerOptions.commonScalacOptions,
  libraryDependencies ++= Seq(
    compilerPlugin("org.scalamacros" %% "paradise" % "2.1.1" cross CrossVersion.patch),
    compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
    "com.github.mpilquist" %%% "simulacrum" % "0.15.0",
    "org.typelevel" %%% "machinist" % "0.6.6"
  ),
  fork in test := true,
  parallelExecution in Test := false
) ++ CompilerOptions.noFatalWarningsInDoc ++ CompilerOptions.warnUnusedImport ++ CompilerOptions.update2_12

lazy val coreSettings = Seq(
  coverageMinimum := 90,
  coverageFailOnMinimum := false
) ++ commonSettings ++ Publishing.publishSettings

lazy val commonJvmSettings = Seq(
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
  // currently sbt-doctest doesn't work in JS builds, so this has to go in the
  // JVM settings. https://github.com/tkawachi/sbt-doctest/issues/52
)

lazy val commonJsSettings = Seq(
  scalacOptions += {
    val tagOrHash =
      if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lines_!.head
      else "v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
    val a = (baseDirectory in LocalRootProject).value.toURI.toString
    val g = "https://raw.githubusercontent.com/typelevel/cats/" + tagOrHash
    s"-P:scalajs:mapSourceURI:$a->$g/"
  },
  scalaJSStage in Global := FastOptStage,
  parallelExecution := false,
  jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
  // Only used for scala.js for now
  Publishing.botBuild := scala.sys.env.get("TRAVIS").isDefined,
  // batch mode decreases the amount of memory needed to compile scala.js code
  scalaJSOptimizerOptions := scalaJSOptimizerOptions.value.withBatchMode(Publishing.botBuild.value),
  doctestGenTests := Seq.empty
)

// projects

lazy val catsVersion = "1.6.0"

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(moduleName := "cats-mtl-core", name := "Cats MTL core")
  .settings(coreSettings: _*)
  .settings(includeGeneratedSrc)
  .settings(
    libraryDependencies += "com.lihaoyi" %% "acyclic" % "0.1.8" % "provided",
    autoCompilerPlugins := true,
    addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.8"),
    scalacOptions += "-P:acyclic:force",
    libraryDependencies += "org.typelevel" %%% "cats-core" % catsVersion
  )
  .jsSettings(commonJsSettings: _*)
  .jvmSettings(commonJvmSettings: _*)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

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

lazy val docs = project
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(ScalaUnidocPlugin)
  .settings(moduleName := "cats-mtl-docs")
  .settings(coreSettings)
  .settings(Publishing.noPublishSettings)
  .settings(docSettings)
  .settings(commonJvmSettings)
  .dependsOn(coreJVM)

lazy val laws = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core)
  .settings(moduleName := "cats-mtl-laws", name := "Cats MTL laws")
  .settings(coreSettings: _*)
  .settings(
    libraryDependencies += "org.typelevel" %%% "cats-laws" % catsVersion
  )
  .jsSettings(commonJsSettings: _*)
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(coverageEnabled := false)

lazy val lawsJVM = laws.jvm
lazy val lawsJS = laws.js

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core, laws)
  .settings(moduleName := "cats-mtl-tests")
  .settings(coreSettings: _*)
  .settings(
    libraryDependencies += "org.typelevel" %%% "cats-testkit" % catsVersion
  )
  .settings(Publishing.noPublishSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .jvmSettings(commonJvmSettings: _*)

lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js

lazy val catsMtl = project
  .in(file("."))
  .settings(moduleName := "root")
  .settings(Publishing.noPublishSettings)
  .aggregate(coreJVM, coreJS, lawsJVM, lawsJS, docs, testsJVM, testsJS)
