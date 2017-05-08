import sbt._, Keys._

object CompilerOptions {

  val commonScalacOptions = Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
//    "-Xlint",
    "-Yno-adapted-args",
//    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  )

  val warnUnusedImport = Def.settings(
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) =>
          Seq()
        case Some((2, n)) if n >= 11 =>
          Seq("-Ywarn-unused-import")
      }
    },
    scalacOptions in(Compile, console) ~= {
      _.filterNot("-Ywarn-unused-import" == _)
    },
    scalacOptions in(Test, console) := (scalacOptions in(Compile, console)).value
  )

  val update2_12 = Def.settings(
    scalacOptions -= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 12)) => "-Yinline-warnings"
        case _ => ""
      }
    }
  )

  val noFatalWarningsInDoc = Def.settings(
    scalacOptions in(Compile, doc) :=
      (scalacOptions in(Compile, doc)).value.filter(_ != "-Xfatal-warnings")
  )

}