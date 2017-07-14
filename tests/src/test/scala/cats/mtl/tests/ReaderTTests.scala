package cats
package mtl
package tests

import cats.arrow.FunctionK
import cats.data._
import cats.instances.all._
import cats.laws.discipline.SerializableTests
import cats.mtl.instances.local._
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

  implicit def eqKleisliId[A, B](implicit arb: Arbitrary[A], ev: Eq[B]): Eq[Kleisli[Id, A, B]] =
    eqKleisli[Id, A, B]

  implicit def catsLawsArbitraryForKleisliId[A, B](implicit F: Arbitrary[A => B]): Arbitrary[Kleisli[Id, A, B]] =
    Arbitrary(F.arbitrary.map(Kleisli[Id, A, B]))

  {
    implicit val monadLayerControl: MonadLayerControl[ReaderTC[Option, String]#l, Option] =
      cats.mtl.instances.readert.readerMonadLayerControl[Option, String]
    checkAll("ReaderT[Option, String, ?]",
      MonadLayerControlTests[ReaderTC[Option, String]#l, Option].monadLayerControl[Boolean, Boolean])
    checkAll("MonadLayerControl[ReaderT[Option, String, ?], Option]",
      SerializableTests.serializable(monadLayerControl))
  }

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

  type ReaderTStringOverReaderTStringOverOption[A] = ReaderT[ReaderTC[Option, String]#l, Int, A]

  {
    checkAll("Reader[String, ?]",
      ApplicativeLocalTests[ReaderTC[Id, String]#l, String].applicativeLocal[String])
    checkAll("FunctorLocal[Reader[String, ?], String]",
      SerializableTests.serializable(ApplicativeLocal[ReaderTC[Id, String]#l, String]))
  }

  {
    checkAll("ReaderT[Option, String, ?]",
      ApplicativeLocalTests[ReaderTC[Option, String]#l, String].applicativeLocal[String])
    checkAll("FunctorLocal[ReaderT[Option, String, ?], String]",
      SerializableTests.serializable(ApplicativeLocal[ReaderTC[Option, String]#l, String]))
  }

}
