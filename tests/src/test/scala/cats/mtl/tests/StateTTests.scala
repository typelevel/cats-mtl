package cats
package mtl
package tests

import cats.arrow.FunctionK
import cats.data.{Kleisli, State, StateT}
import cats.laws.discipline.SerializableTests
import cats.mtl.laws.discipline.{ApplicativeAskTests, FunctorTellTests, MonadStateTests}
import cats.laws.discipline.arbitrary._
import cats.laws.discipline._
import cats.laws.discipline.eq._
import org.scalacheck.{Arbitrary, Gen}
import cats.instances.all._

class StateTTestsBase extends BaseSuite {
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
}

class StateTTests extends StateTTestsBase {
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


  locally {
    implicit def slowCatsLawsEqForFn1[A, B](implicit A: Arbitrary[A], B: Eq[B]): Eq[A => B] =
      tweakableCatsLawsEqForFn1[A, B](20)

    checkAll("ReaderT[StateT[Option, String, ?], Int, String]",
      MonadStateTests[ReaderTIntOverStateTStringOverOption, String]
        .monadState[String])
    checkAll("MonadState[ReaderT[StateT[Option, String, ?], Int, ?]]",
      SerializableTests.serializable(MonadState[ReaderTIntOverStateTStringOverOption, String]))
  }

  checkAll("WriterT[StateT[Option, String, ?], Int, String]",
    MonadStateTests[WriterTIntOverStateTStringOverOption, String]
      .monadState[String])
  checkAll("MonadState[WriterT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(MonadState[WriterTIntOverStateTStringOverOption, String]))

  // TODO: can't test StateT nested with StateT for now for some reason, investigate later

  checkAll("EitherT[StateT[Option, String, ?], Int, String]",
    MonadStateTests[EitherTIntOverStateTStringOverOption, String]
      .monadState[String])
  checkAll("MonadState[EitherT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(MonadState[EitherTIntOverStateTStringOverOption, String]))

  checkAll("OptionT[StateT[Option, String, ?], Int, String]",
    MonadStateTests[OptionTOverStateTStringOverOption, String]
      .monadState[String])
  checkAll("MonadState[OptionT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(MonadState[OptionTOverStateTStringOverOption, String]))

}
