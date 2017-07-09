package cats
package mtl
package tests

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.listen._
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline._
import org.scalacheck._

class ReaderTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](implicit arb: Arbitrary[A], ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] =
    Eq.by((x: (Kleisli[F, A, B])) => x.run)

  {
    implicit val applicativeLayerFunctor: ApplicativeLayerFunctor[ReaderTC[Option, String]#l, Option] =
      cats.mtl.instances.readert.readerApplicativeLayerFunctor[Option, String]
    checkAll("ReaderT[Option, String, ?]",
      ApplicativeLayerFunctorTests[ReaderTC[Option, String]#l, Option].applicativeLayerFunctor[Boolean, Boolean])
    checkAll("ApplicativeLayerFunctor[ReaderT[Option, String, ?], Option]",
      SerializableTests.serializable(applicativeLayerFunctor))
  }

  {
    implicit val functorLayerFunctor: FunctorLayerFunctor[ReaderTC[Option, String]#l, Option] =
      cats.mtl.instances.readert.readerFunctorLayerFunctor[Option, String]
    checkAll("ReaderT[Option, String, ?]",
      FunctorLayerFunctorTests[ReaderTC[Option, String]#l, Option].functorLayerFunctor[Boolean])
    checkAll("FunctorLayerFunctor[ReaderT[Option, String, ?], Option]",
      SerializableTests.serializable(functorLayerFunctor))
  }
}
