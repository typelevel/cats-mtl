// project info

organization in ThisBuild := "org.typelevel"

// aliases

addCommandAlias("buildJVM", "catsMtlJVM/test")

addCommandAlias("validateJVM", ";scalastyle;scalafmt::test;test:scalafmt::test;buildJVM;mimaReportBinaryIssues;makeMicrosite")

addCommandAlias("validateJS", ";catsMtlJS/compile;testsJS/test;js/test")

addCommandAlias("validate", ";clean;validateJS;validateJVM")

addCommandAlias("gitSnapshots", ";set version in ThisBuild := git.gitDescribedVersion.value.get + \"-SNAPSHOT\"")

sbtPlugin := true

publishMavenStyle := false

// projects

val core = crossProject.crossType(CrossType.Pure)
  .settings(moduleName := "cats-mtl-core", name := "Cats MTL core")
  .settings(Settings.coreSettings:_*)
  .settings(Settings.includeGeneratedSrc)
  .settings(Dependencies.acyclic)
  .settings(Dependencies.catsCore)
  .settings(Dependencies.scalaCheck)
  .jsSettings(Settings.commonJsSettings:_*)
  .jvmSettings(Settings.commonJvmSettings:_*)

val coreJVM = core.jvm
val coreJS = core.js

val docs = project
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(ScalaUnidocPlugin)
  .settings(moduleName := "cats-mtl-docs")
  .settings(Settings.coreSettings)
  .settings(Publishing.noPublishSettings)
  .settings(ghpages.settings)
  .settings(Docs.docSettings)
  .settings(tutScalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))))
  .settings(Settings.commonJvmSettings)
  .dependsOn(coreJVM)

val laws = crossProject.crossType(CrossType.Pure)
  .dependsOn(core)
  .settings(moduleName := "cats-mtl-laws", name := "Cats MTL laws")
  .settings(Settings.coreSettings:_*)
  .settings(Dependencies.catsBundle:_*)
  .settings(Dependencies.discipline:_*)
  .settings(Dependencies.catalystsAndScalatest: _*)
  .jsSettings(Settings.commonJsSettings:_*)
  .jvmSettings(Settings.commonJvmSettings:_*)
  .jsSettings(coverageEnabled := false)

val lawsJVM = laws.jvm
val lawsJS = laws.js

val tests = crossProject.crossType(CrossType.Pure)
  .dependsOn(core, laws)
  .settings(moduleName := "cats-mtl-tests")
  .settings(Settings.coreSettings:_*)
  .settings(Dependencies.catsBundle:_*)
  .settings(Dependencies.discipline:_*)
  .settings(Dependencies.scalaCheck:_*)
  .settings(Publishing.noPublishSettings:_*)
  .settings(Dependencies.catalystsAndScalatest:_*)
  .jsSettings(Settings.commonJsSettings:_*)
  .jvmSettings(Settings.commonJvmSettings:_*)

val testsJVM = tests.jvm
val testsJS = tests.js

// cats-mtl-js is JS-only
val js = project
  .dependsOn(coreJS, testsJS % "test-internal -> test")
  .settings(moduleName := "cats-mtl-js")
  .settings(Settings.coreSettings:_*)
  .settings(Settings.commonJsSettings:_*)
  .settings(Publishing.noPublishSettings)
  .enablePlugins(ScalaJSPlugin)

// cats-mtl-jvm is JVM-only
val jvm = project
  .dependsOn(coreJVM, testsJVM % "test-internal -> test")
  .settings(moduleName := "cats-mtl-jvm")
  .settings(Settings.coreSettings:_*)
  .settings(Settings.commonJvmSettings:_*)
  .settings(Publishing.noPublishSettings)

val catsMtlJVM = project.in(file(".catsJVM"))
  .settings(moduleName := "cats-mtl")
  .settings(Settings.coreSettings)
  .settings(Settings.commonJvmSettings)
  .settings(Publishing.noPublishSettings)
  .aggregate(coreJVM, lawsJVM, testsJVM, jvm, docs)
  .dependsOn(coreJVM, lawsJVM, testsJVM % "test-internal -> test", jvm)

val catsMtlJS = project.in(file(".catsJS"))
  .settings(moduleName := "cats-mtl")
  .settings(Settings.coreSettings)
  .settings(Settings.commonJsSettings)
  .settings(Publishing.noPublishSettings)
  .aggregate(coreJS, lawsJS, testsJS, js)
  .dependsOn(coreJS, lawsJS, testsJS % "test-internal -> test", js)
  .enablePlugins(ScalaJSPlugin)

val catsMtl = project.in(file("."))
  .settings(moduleName := "root")
  .settings(Settings.coreSettings)
  .settings(Publishing.noPublishSettings)
  .aggregate(catsMtlJVM, catsMtlJS)
  .dependsOn(catsMtlJVM, catsMtlJS, testsJVM % "test-internal -> test")

