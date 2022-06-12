ThisBuild / tlBaseVersion := "1.2"
ThisBuild / startYear := Some(2021)
ThisBuild / developers := List(
  tlGitHubDev("SystemFw", "Fabio Labella"),
  tlGitHubDev("andyscott", "Andy Scott"),
  tlGitHubDev("kailuowang", "Kailuo Wang"),
  tlGitHubDev("djspiewak", "Daniel Spiewak"),
  tlGitHubDev("LukaJCB", "Luka Jacobowitz"),
  tlGitHubDev("edmundnoble", "Edmund Noble")
)

val Scala213 = "2.13.8"

ThisBuild / crossScalaVersions := Seq("3.1.1", "2.12.15", Scala213)
ThisBuild / tlVersionIntroduced := Map("3" -> "1.2.1")

lazy val commonJvmSettings = Seq(
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
)

lazy val commonJsSettings = Seq(
  doctestGenTests := Seq.empty
)

val CatsVersion = "2.7.0"

lazy val root = tlCrossRootProject.aggregate(core, laws, tests)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(name := "cats-mtl")
  .settings(
    libraryDependencies += "org.typelevel" %%% "cats-core" % CatsVersion
  )
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)

lazy val laws = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core)
  .settings(name := "cats-mtl-laws")
  .settings(libraryDependencies += "org.typelevel" %%% "cats-laws" % CatsVersion)
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(core, laws)
  .enablePlugins(NoPublishPlugin)
  .settings(name := "cats-mtl-tests")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-testkit" % CatsVersion,
      "org.scalameta" %%% "munit" % "0.7.29",
      "org.typelevel" %%% "discipline-munit" % "1.0.9"))
  .jsSettings(commonJsSettings)
  .jvmSettings(commonJvmSettings)

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
