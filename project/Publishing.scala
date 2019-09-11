import sbtrelease.ReleasePlugin.autoImport._
import xerial.sbt.Sonatype.autoImport._

import sbt._, Keys._
import sbtrelease.ReleaseStateTransformations._

object Publishing {

  lazy val botBuild =
    settingKey[Boolean]("Build by TravisCI instead of local dev environment")

  lazy val sharedPublishSettings = Seq(
    releaseCrossBuild := true,
    // releaseTagName := tagName.value,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := Function.const(false),
    publishTo := sonatypePublishToBundle.value
  )

  lazy val sharedReleaseProcess = Seq(
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommand("validate"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      ReleaseStep(action = Command.process("sonatypeBundleRelease", _), enableCrossBuild = true),
      pushChanges)
  )

  lazy val publishSettings = Seq(
    homepage := Some(url("https://github.com/typelevel/cats-mtl")),
    licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
    scmInfo := Some(ScmInfo(url("https://github.com/typelevel/cats-mtl"), "scm:git:git@github.com:typelevel/cats-mtl.git")),
    autoAPIMappings := true,
    apiURL := None,
    pomExtra :=
      <developers>
        <developer>
          <id>SystemFw</id>
          <name>Fabio Labella</name>
          <url>https://github.com/SystemFw/</url>
        </developer>
        <developer>
          <id>andyscott</id>
          <name>Andy Scott</name>
          <url>https://github.com/andyscott/</url>
        </developer>
        <developer>
          <id>kailuowang</id>
          <name>Kailuo Wang</name>
          <url>https://github.com/kailuowang/</url>
        </developer>
        <developer>
          <id>djspiewak</id>
          <name>Daniel Spiewak</name>
          <url>https://github.com/djspiewak/</url>
        </developer>
        <developer>
          <id>LukaJCB</id>
          <name>Luka Jacobowitz</name>
          <url>https://github.com/LukaJCB/</url>
        </developer>
        <developer>
          <id>edmundnoble</id>
          <name>Edmund Noble</name>
          <url>https://github.com/edmundnoble/</url>
        </developer>
    </developers>
  ) ++ credentialSettings ++ sharedPublishSettings ++ sharedReleaseProcess

  lazy val noPublishSettings = Seq(
    publish := (()),
    publishLocal := (()),
    publishArtifact := false,
    publish / skip := true
  )

  lazy val credentialSettings = Seq(
    // For Travis CI - see http://www.cakesolutions.net/teamblogs/publishing-artefacts-to-oss-sonatype-nexus-using-sbt-and-travis-ci
    credentials ++=
      (for {
        username <- Option(System.getenv().get("SONATYPE_USERNAME"))
        password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
      } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq,
    credentials += Credentials(
      Option(System.getProperty("build.publish.credentials")) map (new File(_)) getOrElse (Path.userHome / ".ivy2" / ".credentials")
    )
  )


}
