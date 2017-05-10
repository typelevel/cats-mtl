package cats
package mtl

import catalysts.Platform
import cats.syntax.{EqOps, EqSyntax}
import org.scalactic.anyvals.{PosInt, PosZDouble, PosZInt}
import org.scalatest.prop.Configuration
import org.scalatest.{FunSuite, Matchers}
import org.typelevel.discipline.scalatest.Discipline

class BaseSuite extends FunSuite
  with Matchers
  with Configuration
  with StrictCatsEquality
  with EqSyntax
  with Discipline {

  // disable Eq syntax (by making `catsSyntaxEq` not implicit), since it collides
  // with scalactic's equality
  override def catsSyntaxEq[A: Eq](a: A): EqOps[A] = new EqOps[A](a)

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    checkConfiguration

  lazy val checkConfiguration: PropertyCheckConfiguration =
    PropertyCheckConfiguration(
      minSuccessful = if (Platform.isJvm) PosInt(50) else PosInt(5),
      maxDiscardedFactor = if (Platform.isJvm) PosZDouble(5.0) else PosZDouble(50.0),
      minSize = PosZInt(0),
      sizeRange = if (Platform.isJvm) PosZInt(10) else PosZInt(5),
      workers = PosInt(1))

  lazy val slowCheckConfiguration: PropertyCheckConfiguration =
    if (Platform.isJvm) checkConfiguration
    else PropertyCheckConfiguration(minSuccessful = 1, sizeRange = 1)

}
