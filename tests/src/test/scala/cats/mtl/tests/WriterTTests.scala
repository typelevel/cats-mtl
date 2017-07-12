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

class WriterTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](implicit arb: Arbitrary[A], ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] =
    Eq.by((x: (Kleisli[F, A, B])) => x.run)

  implicit def catsLawArbitraryForStateT[F[_], S, A](implicit F: Arbitrary[F[S => F[(S, A)]]]): Arbitrary[StateT[F, S, A]] = {
    Arbitrary(F.arbitrary.map(StateT.applyF))
  }

  implicit def stateTEq[F[_], S, A](implicit S: Arbitrary[S], FSA: Eq[F[(S, A)]], F: FlatMap[F]): Eq[StateT[F, S, A]] = {
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state =>
      s => state.run(s))
  }

  checkAll("WriterT[Option, String, String]",
    FunctorListenTests[WriterTC[Option, String]#l, String].functorListen[String, String])
  checkAll("FunctorListen[WriterT[Option, String, ?]]",
    SerializableTests.serializable(FunctorListen[WriterTC[Option, String]#l, String]))

  locally {
    import cats.mtl.instances.all._

    type WriterTStringOverWriterTStringOverOption[A] = WriterT[WriterTC[Option, String]#l, List[Int], A]
    type ReaderTStringOverWriterTStringOverOption[A] = ReaderT[WriterTC[Option, String]#l, List[Int], A]
    type StateTStringOverWriterTStringOverOption[A] = StateT[WriterTC[Option, String]#l, List[Int], A]

    checkAll("WriterT[WriterTC[Option, String]#l, List[Int], String]",
      FunctorListenTests[WriterTStringOverWriterTStringOverOption, String].functorListen[String, String])
    checkAll("FunctorListen[WriterT[WriterTC[Option, String]#l, List[Int], String]",
      SerializableTests.serializable(FunctorListen[WriterTStringOverWriterTStringOverOption, String]))

    checkAll("ReaderT[WriterTC[Option, String]#l, List[Int], String]",
      FunctorListenTests[ReaderTStringOverWriterTStringOverOption, String].functorListen[String, String])
    checkAll("FunctorListen[ReaderT[WriterTC[Option, String]#l, List[Int], String]",
      SerializableTests.serializable(FunctorListen[ReaderTStringOverWriterTStringOverOption, String]))

    checkAll("StateT[WriterTC[Option, String]#l, List[Int], String]",
      FunctorListenTests[StateTStringOverWriterTStringOverOption, String].functorListen[String, String])
    checkAll("FunctorListen[StateT[WriterTC[Option, String]#l, List[Int], String]",
      SerializableTests.serializable(FunctorListen[StateTStringOverWriterTStringOverOption, String]))
  }

  {
    implicit val monadLayerFunctor: MonadLayerFunctor[WriterTC[Option, String]#l, Option] =
      cats.mtl.instances.writert.writerMonadLayerControl[Option, String]
    checkAll("WriterT[Option, String, ?]",
      MonadLayerFunctorTests[WriterTC[Option, String]#l, Option].monadLayerFunctor[Boolean, Boolean])
    checkAll("MonadLayerFunctor[WriterT[Option, String, ?], Option]",
      SerializableTests.serializable(monadLayerFunctor))
  }

  {
    implicit val applicativeLayerFunctor: ApplicativeLayerFunctor[WriterTC[Option, String]#l, Option] =
      cats.mtl.instances.writert.writerApplicativeLayerFunctor[Option, String]
    checkAll("WriterT[Option, String, ?]",
      ApplicativeLayerFunctorTests[WriterTC[Option, String]#l, Option].applicativeLayerFunctor[Boolean, Boolean])
    checkAll("ApplicativeLayerFunctor[WriterT[Option, String, ?], Option]",
      SerializableTests.serializable(applicativeLayerFunctor))
  }

  {
    implicit val functorLayerFunctor: FunctorLayerFunctor[WriterTC[Option, String]#l, Option] =
      cats.mtl.instances.writert.writerFunctorLayerFunctor[Option, String]
    checkAll("WriterT[Option, String, ?]",
      FunctorLayerFunctorTests[WriterTC[Option, String]#l, Option].functorLayerFunctor[Boolean])
    checkAll("FunctorLayerFunctor[WriterT[Option, String, ?], Option]",
      SerializableTests.serializable(functorLayerFunctor))
  }
}
