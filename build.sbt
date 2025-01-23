ThisBuild / tlBaseVersion := "1.5"
ThisBuild / startYear := Some(2021)
ThisBuild / developers := List(
  tlGitHubDev("SystemFw", "Fabio Labella"),
  tlGitHubDev("andyscott", "Andy Scott"),
  tlGitHubDev("kailuowang", "Kailuo Wang"),
  tlGitHubDev("djspiewak", "Daniel Spiewak"),
  tlGitHubDev("LukaJCB", "Luka Jacobowitz"),
  tlGitHubDev("edmundnoble", "Edmund Noble")
)

val Scala213 = "2.13.16"

ThisBuild / crossScalaVersions := Seq("3.3.4", "2.12.20", Scala213)
ThisBuild / tlVersionIntroduced := Map("3" -> "1.2.1")

lazy val commonJvmSettings = Seq(
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
)

lazy val commonJsSettings = Seq(
  doctestGenTests := Seq.empty
)

// cats-mtl 1.5.0 switches to Scala Native 0.5.
// Therefore `tlVersionIntroduced` should be reset to 1.5.0 for all scala versions in all native cross-projects.
val commonNativeTlVersionIntroduced = List("2.12", "2.13", "3").map(_ -> "1.5.0").toMap

lazy val commonNativeSettings = Seq(
  doctestGenTests := Seq.empty,
  tlVersionIntroduced := commonNativeTlVersionIntroduced
)

val CatsVersion = "2.13.0"

lazy val root = tlCrossRootProject.aggregate(core, laws, tests, unidocs)

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .settings(name := "cats-mtl")
  .settings(
    libraryDependencies += "org.typelevel" %%% "cats-core" % CatsVersion
  )
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)
  .nativeSettings(commonNativeSettings)

lazy val laws = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core)
  .settings(name := "cats-mtl-laws")
  .settings(libraryDependencies += "org.typelevel" %%% "cats-laws" % CatsVersion)
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)
  .nativeSettings(commonNativeSettings)

lazy val tests = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .dependsOn(core, laws)
  .enablePlugins(NoPublishPlugin)
  .settings(name := "cats-mtl-tests")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-testkit" % CatsVersion,
      "org.scalameta" %%% "munit" % "1.0.0",
      "org.typelevel" %%% "discipline-munit" % "2.0.0"))
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)
  .nativeSettings(commonNativeSettings)

lazy val unidocs = project
  .in(file("unidocs"))
  .enablePlugins(TypelevelUnidocPlugin)
  .settings(
    name := "cats-mtl-docs",
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(core.jvm, laws.jvm)
  )

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .settings(
    tlFatalWarnings := false,
    laikaConfig ~= (_.withRawContent)
  )
  .dependsOn(core.jvm)
