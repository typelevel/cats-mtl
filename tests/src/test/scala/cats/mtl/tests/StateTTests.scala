/*
 * Copyright 2020 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats
package mtl
package tests

import cats.arrow.FunctionK
import cats.data.{Kleisli, State, StateT}
import cats.laws.discipline.SerializableTests
import cats.mtl.laws.discipline.StatefulTests
import cats.laws.discipline.arbitrary._
import cats.laws.discipline._
import cats.laws.discipline.eq._
import org.scalacheck.{Arbitrary, Gen}

class StateTTestsBase extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(
      Gen.oneOf(
        new (Option ~> Option) {
          def apply[A](fa: Option[A]): Option[A] = None
        },
        FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](
      implicit arb: Arbitrary[A],
      ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] =
    Eq.by((x: (Kleisli[F, A, B])) => x.run)

  implicit def stateTEq[F[_], S, A](
      implicit S: Arbitrary[S],
      FSA: Eq[F[(S, A)]],
      F: FlatMap[F]): Eq[StateT[F, S, A]] =
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state => s => state.run(s))
}

class StateTTests extends StateTTestsBase {
  checkAll("State[String, String]", StatefulTests[StateC[String]#l, String].stateful[String])
  checkAll(
    "Stateful[State[String, ?]]",
    SerializableTests.serializable(Stateful[StateC[String]#l, String]))

  checkAll(
    "StateT[Option, String, String]",
    StatefulTests[StateTC[Option, String]#l, String].stateful[String])
  checkAll(
    "Stateful[StateT[Option, String, ?]]",
    SerializableTests.serializable(Stateful[StateTC[Option, String]#l, String]))

  Stateful[State[Int, *], Int]

  {
    implicit def slowCatsLawsEqForFn1[A, B](implicit A: Arbitrary[A], B: Eq[B]): Eq[A => B] =
      tweakableCatsLawsEqForFn1[A, B](20)

    checkAll(
      "ReaderT[StateT[Option, String, ?], Int, String]",
      StatefulTests[ReaderTIntOverStateTStringOverOption, String].stateful[String])
    checkAll(
      "Stateful[ReaderT[StateT[Option, String, ?], Int, ?]]",
      SerializableTests.serializable(Stateful[ReaderTIntOverStateTStringOverOption, String]))
  }

  checkAll(
    "WriterT[StateT[Option, String, ?], Int, String]",
    StatefulTests[WriterTIntOverStateTStringOverOption, String].stateful[String])
  checkAll(
    "Stateful[WriterT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(Stateful[WriterTIntOverStateTStringOverOption, String]))

  // TODO: can't test StateT nested with StateT for now for some reason, investigate later

  checkAll(
    "EitherT[StateT[Option, String, ?], Int, String]",
    StatefulTests[EitherTIntOverStateTStringOverOption, String].stateful[String])
  checkAll(
    "Stateful[EitherT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(Stateful[EitherTIntOverStateTStringOverOption, String]))

  checkAll(
    "OptionT[StateT[Option, String, ?], Int, String]",
    StatefulTests[OptionTOverStateTStringOverOption, String].stateful[String])
  checkAll(
    "Stateful[OptionT[StateT[Option, String, ?], Int, ?]]",
    SerializableTests.serializable(Stateful[OptionTOverStateTStringOverOption, String]))

}
