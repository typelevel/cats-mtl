package cats
package mtl
package tests

import catalysts.Platform
import cats.data._
import cats.syntax.{EqOps, EqSyntax}
import org.scalactic.anyvals.{PosInt, PosZDouble, PosZInt}
import org.scalatest.prop.Configuration
import org.scalatest.{FunSuite, Matchers}
import org.typelevel.discipline.scalatest.Discipline

abstract class BaseSuite extends FunSuite
  with Matchers
  with Configuration
  with StrictCatsEquality
  with EqSyntax
  with Discipline {

  protected type ReaderStr[M[_], A] = ReaderT[M, String, A]
  protected type ReaderStrId[A] = ReaderT[Id, String, A]
  protected type ReaderInt[M[_], A] = ReaderT[M, Int, A]
  protected type ReaderIntId[A] = Reader[Int, A]
  protected type ReaderStrInt[A] = ReaderStr[ReaderIntId, A]
  protected type ReaderStrFuncInt[A] = ReaderStr[FunctionC[Int]#l, A]

  protected type EitherTStr[M[_], A] = EitherT[M, String, A]
  protected type EitherStrId[A] = EitherT[Id, String, A]
  protected type EitherTInt[M[_], A] = EitherT[M, Int, A]
  protected type EitherTIntId[A] = EitherT[Id, Int, A]
  protected type EitherTStrEitherTInt[A] = EitherTStr[EitherTIntId, A]

  protected type StateTStr[M[_], A] = StateT[M, String, A]
  protected type StateStrId[A] = StateT[Id, String, A]
  protected type StateTInt[M[_], A] = StateT[M, Int, A]
  protected type StateTIntId[A] = StateT[Id, Int, A]
  protected type StateTStrStateTInt[A] = StateTStr[StateTIntId, A]

  protected type WriterTStr[M[_], A] = WriterT[M, String, A]
  protected type WriterStrId[A] = WriterT[Id, String, A]
  protected type WriterTInt[M[_], A] = WriterT[M, Vector[Int], A]
  protected type WriterTIntId[A] = WriterT[Id, Vector[Int], A]
  protected type WriterTStrWriterTInt[A] = WriterTStr[WriterTIntId, A]
  protected type WriterTStrTupleInt[A] = WriterTStr[TupleC[Vector[Int]]#l, A]

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
