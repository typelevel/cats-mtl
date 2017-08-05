package cats
package mtl
package tests

import cats.arrow.FunctionK
import cats.data.{Kleisli, State, StateT}
import cats.laws.discipline.SerializableTests
import cats.mtl.laws.discipline.{ApplicativeAskTests, FunctorTellTests, MonadLayerControlTests, MonadStateTests}
import cats.laws.discipline.arbitrary._
import cats.laws.discipline._
import cats.laws.discipline.eq._
import org.scalacheck.{Arbitrary, Gen}
import cats.mtl.instances.state._
import cats.mtl.instances.local._
import cats.mtl.instances.readert._
import cats.mtl.instances.writert._
import cats.mtl.instances.optiont._
import cats.mtl.instances.eithert._
import cats.mtl.hierarchy.base._
import cats.instances.all._
import cats.mtl.MonadLayerControl.Aux

class StateTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](implicit arb: Arbitrary[A], ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] = {
    Eq.by((x: (Kleisli[F, A, B])) => x.run)
  }

  implicit def stateTEq[F[_], S, A](implicit S: Arbitrary[S], FSA: Eq[F[(S, A)]], F: FlatMap[F]): Eq[StateT[F, S, A]] = {
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state =>
      s => state.run(s))
  }

  {
    implicit val monadLayerControl: MonadLayerControl.Aux[StateTC[Option, String]#l, Option, TupleC[String]#l] =
      cats.mtl.instances.statet.stateMonadLayerControl[Option, String]
    checkAll("StateT[Option, String, ?]",
      MonadLayerControlTests[StateTC[Option, String]#l, Option, TupleC[String]#l]
        .monadLayerControl[Boolean, Boolean])
    checkAll("MonadLayerControl[StateT[Option, String, ?], Option]",
      SerializableTests.serializable(monadLayerControl))
  }

  checkAll("State[String, String]",
    MonadStateTests[StateC[String]#l, String]
      .monadState[String])
  checkAll("MonadState[State[String, ?]]",
    SerializableTests.serializable(MonadState[StateC[String]#l, String]))

  checkAll("StateT[Option, String, String]",
    MonadStateTests[StateTC[Option, String]#l, String]
      .monadState[String])
  checkAll("MonadState[StateT[Option, String, ?]]",
    SerializableTests.serializable(MonadState[StateTC[Option, String]#l, String]))

  checkAll("StateT[Option, String, String]",
    FunctorTellTests[StateTC[Option, String]#l, String]
      .functorTell[String])
  checkAll("FunctorTell[StateT[Option, String, ?]]",
    SerializableTests.serializable(FunctorTell[StateTC[Option, String]#l, String]))

  checkAll("StateT[Option, String, String]",
    ApplicativeAskTests[StateTC[Option, String]#l, String]
      .applicativeAsk[String])
  checkAll("ApplicativeAsk[StateT[Option, String, ?]]",
    SerializableTests.serializable(ApplicativeAsk[StateTC[Option, String]#l, String]))

  locally {
    implicit def slowCatsLawsEqForFn1[A, B](implicit A: Arbitrary[A], B: Eq[B]): Eq[A => B] =
      tweakableCatsLawsEqForFn1[A, B](20)

    checkAll("ReaderT[StateT[Option, String, ?], Int, String]",
      MonadStateTests[ReaderTIntOverStateTStringOverOption, String]
        .monadState[String])
    checkAll("MonadState[ReaderT[StateT[Option, String, ?], Int, ?]]",
      SerializableTests.serializable(MonadState[ReaderTIntOverStateTStringOverOption, String]))

    checkAll("ReaderT[StateT[Option, String, ?], Int, String]",
      FunctorTellTests[ReaderTIntOverStateTStringOverOption, String]
        .functorTell[String])
    checkAll("FunctorTell[ReaderT[StateT[Option, String, ?], Int, ?]]",
      SerializableTests.serializable(FunctorTell[ReaderTIntOverStateTStringOverOption, String]))

    checkAll("ReaderT[StateT[Option, String, ?], Int, String]",
      ApplicativeAskTests[ReaderTIntOverStateTStringOverOption, String]
        .applicativeAsk[String])
    checkAll("ApplicativeAsk[ReaderT[StateT[Option, String, ?], Int, ?]]",
      SerializableTests.serializable(ApplicativeAsk[ReaderTIntOverStateTStringOverOption, String]))
  }

  checkAll("WriterT[StateT[Option, String, ?], Int, String]",
    MonadStateTests[WriterTIntOverStateTStringOverOption, String]
      .monadState[String])
  checkAll("MonadState[WriterT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(MonadState[WriterTIntOverStateTStringOverOption, String]))

  checkAll("WriterT[StateT[Option, String, ?], Int, String]",
    FunctorTellTests[WriterTIntOverStateTStringOverOption, String]
      .functorTell[String])
  checkAll("FunctorTell[WriterT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(FunctorTell[WriterTIntOverStateTStringOverOption, String]))

  checkAll("WriterT[StateT[Option, String, ?], Int, String]",
    ApplicativeAskTests[WriterTIntOverStateTStringOverOption, String]
      .applicativeAsk[String])
  checkAll("ApplicativeAsk[WriterT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(ApplicativeAsk[WriterTIntOverStateTStringOverOption, String]))

  // TODO: can't test StateT nested with StateT for now for some reason, investigate later

  checkAll("EitherT[StateT[Option, String, ?], Int, String]",
    MonadStateTests[EitherTIntOverStateTStringOverOption, String]
      .monadState[String])
  checkAll("MonadState[EitherT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(MonadState[EitherTIntOverStateTStringOverOption, String]))

  checkAll("EitherT[StateT[Option, String, ?], Int, String]",
    FunctorTellTests[EitherTIntOverStateTStringOverOption, String]
      .functorTell[String])
  checkAll("FunctorTell[EitherT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(FunctorTell[EitherTIntOverStateTStringOverOption, String]))

  checkAll("EitherT[StateT[Option, String, ?], Int, String]",
    ApplicativeAskTests[EitherTIntOverStateTStringOverOption, String]
      .applicativeAsk[String])
  checkAll("ApplicativeAsk[EitherT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(ApplicativeAsk[EitherTIntOverStateTStringOverOption, String]))

  checkAll("OptionT[StateT[Option, String, ?], Int, String]",
    MonadStateTests[OptionTOverStateTStringOverOption, String]
      .monadState[String])
  checkAll("MonadState[OptionT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(MonadState[OptionTOverStateTStringOverOption, String]))

  checkAll("OptionT[StateT[Option, String, ?], Int, String]",
    FunctorTellTests[OptionTOverStateTStringOverOption, String]
      .functorTell[String])
  checkAll("FunctorTell[OptionT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(FunctorTell[OptionTOverStateTStringOverOption, String]))

  checkAll("OptionT[StateT[Option, String, ?], Int, String]",
    ApplicativeAskTests[OptionTOverStateTStringOverOption, String]
      .applicativeAsk[String])
  checkAll("ApplicativeAsk[OptionT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(ApplicativeAsk[OptionTOverStateTStringOverOption, String]))

}
