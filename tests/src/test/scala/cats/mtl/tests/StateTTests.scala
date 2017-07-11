package cats
package mtl
package tests

import cats.arrow.FunctionK
import cats.data.StateT
import cats.laws.discipline.SerializableTests
import cats.mtl.laws.discipline.{MonadLayerFunctorTests, MonadStateTests}
import cats.laws.discipline.arbitrary._
import cats.laws.discipline._
import cats.laws.discipline.eq._
import org.scalacheck.{Arbitrary, Gen}
import cats.mtl.instances.state._
import cats.instances.all._

class StateTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(Gen.oneOf(new (Option ~> Option) {
      def apply[A](fa: Option[A]): Option[A] = None
    }, FunctionK.id[Option]))

  implicit def catsLawArbitraryForStateT[F[_], S, A](implicit F: Arbitrary[F[S => F[(S, A)]]]): Arbitrary[StateT[F, S, A]] = {
    Arbitrary(F.arbitrary.map(StateT.applyF))
  }

  implicit def stateTEq[F[_], S, A](implicit S: Arbitrary[S], FSA: Eq[F[(S, A)]], F: FlatMap[F]): Eq[StateT[F, S, A]] = {
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state =>
      s => state.run(s))
  }

  {
    implicit val monadLayerFunctor: MonadLayerFunctor[StateTC[Option, String]#l, Option] =
      cats.mtl.instances.statet.stateMonadLayerControl[Option, String]
    checkAll("StateT[Option, String, ?]",
      MonadLayerFunctorTests[StateTC[Option, String]#l, Option].monadLayerFunctor[Boolean, Boolean])
    checkAll("MonadLayerFunctor[StateT[Option, String, ?], Option]",
      SerializableTests.serializable(monadLayerFunctor))
  }

  checkAll("StateT[Option, String, String]",
    MonadStateTests[StateTC[Option, String]#l, String].monadState[String])
  checkAll("MonadState[StateT[Option, String, ?]]",
    SerializableTests.serializable(MonadState[StateTC[Option, String]#l, String]))

}
