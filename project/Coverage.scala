import org.scalajs.sbtplugin.cross.CrossProject
import scoverage.ScoverageKeys._
import sbt._, Keys._

object Coverage {

  val scoverageSettings = Def.settings(
    coverageMinimum := 90,
    coverageFailOnMinimum := false
  )

  def disableScoverage210Js(crossProject: CrossProject) =
    crossProject
      .jsSettings(
        coverageEnabled := {
          CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((2, 10)) => false
            case _ => coverageEnabled.value
          }
        }
      )

  def disableScoverage210Js: Project ⇒ Project = p =>
    p.settings(
      coverageEnabled := {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, 10)) => false
          case _ => coverageEnabled.value
        }
      }
    )

  def disableScoverage210Jvm(crossProject: CrossProject) =
    crossProject
      .jvmSettings(
        coverageEnabled := {
          CrossVersion.partialVersion(scalaVersion.value) match {
            case Some((2, 10)) => false
            case _ => coverageEnabled.value
          }
        }
      )

}
