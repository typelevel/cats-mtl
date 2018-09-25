import sbt._
import scoverage.ScoverageKeys._

object Coverage {

  val scoverageSettings = Def.settings(
    coverageMinimum := 90,
    coverageFailOnMinimum := false
  )
}
