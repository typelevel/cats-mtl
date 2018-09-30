import sbt.{Def, _}
import Keys._
import org.scalastyle.sbt.ScalastylePlugin._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import com.github.tkawachi.doctest.DoctestPlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport._

object Settings {

  lazy val includeGeneratedSrc: Seq[Setting[_]] = Def.settings {
    mappings in (Compile, packageSrc) ++= {
      val base = (sourceManaged in Compile).value
      (managedSources in Compile).value.map { file =>
        file -> file.relativeTo(base).get.getPath
      }
    }
  }

  val commonSettings = Def.settings(
    incOptions := incOptions.value.withLogRecompileOnMacro(false),
    scalacOptions ++= CompilerOptions.commonScalacOptions,
    Dependencies.sonatypeResolvers,
    Dependencies.bintrayResolvers,
    Dependencies.compilerPlugins,
    Dependencies.simulacrumAndMachinist,
    fork in test := true,
    parallelExecution in Test := false,
    CompilerOptions.noFatalWarningsInDoc,
    CompilerOptions.warnUnusedImport,
    CompilerOptions.update2_12
  )

  val coreSettings =
    commonSettings ++ Publishing.publishSettings ++ Coverage.scoverageSettings

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
}
