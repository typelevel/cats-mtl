import sbt._, Keys._

import xerial.sbt.Sonatype

object Settings extends AutoPlugin {

  // make sure we run *after* sonatype so that we override its shenanigans
  override def requires = Sonatype && plugins.JvmPlugin
  override def trigger = allRequirements

  override def projectSettings = Seq(
    homepage := Some(url("https://typelevel.org/cats-mtl/")),

    scmInfo := Some(
      ScmInfo(
        url("https://github.com/typelevel/cats-mtl"),
        "git@github.com:typelevel/cats-mtl.git")))
}
