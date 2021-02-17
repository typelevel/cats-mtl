/*
 * Copyright 2021 Typelevel
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

package cats.mtl.tests

import cats.arrow.FunctionK
import cats.{~>, Eq, Monad, Monoid}
import cats.data.{ReaderWriterState, ReaderWriterStateT}
import cats.laws.discipline.arbitrary._
import cats.laws.discipline._
import cats.laws.discipline.eq._
import cats.mtl._
import cats.mtl.laws.discipline._
import org.scalacheck.{Arbitrary, Gen}

class ReaderWriterStateTTests extends StateTTestsBase {

  implicit def RWSTEq[F[_], E, L, S, A](
      implicit S: Arbitrary[S],
      E: Arbitrary[E],
      FLSB: Eq[F[(L, S, A)]],
      F: Monad[F]): Eq[ReaderWriterStateT[F, E, L, S, A]] =
    Eq.by[ReaderWriterStateT[F, E, L, S, A], (E, S) => F[(L, S, A)]] { state => (e, s) =>
      state.run(e, s)
    }

  implicit def arbFunctionKTupled[L: Monoid, S]: Arbitrary[(L, S, *) ~> (L, S, *)] =
    Arbitrary(
      Gen.oneOf(
        new ((L, S, *) ~> (L, S, *)) {
          def apply[A](fa: (L, S, A)): (L, S, A) = (Monoid[L].empty, fa._2, fa._3)
        },
        FunctionK.id[(L, S, *)]))

  checkAll(
    "ReaderWriterState[Boolean, Int, String, String]",
    StatefulTests[ReaderWriterState[Boolean, Int, String, *], String].stateful[String])
  checkAll(
    "Stateful[ReaderWriterState[Boolean, Int, String, *]]",
    SerializableTests.serializable(
      Stateful[ReaderWriterState[Boolean, Int, String, *], String]))

  checkAll(
    "ReaderWriterStateT[Option, Boolean, Int, String, String]",
    StatefulTests[ReaderWriterStateT[Option, Boolean, Int, String, *], String].stateful[String])
  checkAll(
    "Stateful[ReaderWriterStateT[Option, Boolean, Int, String, *]]",
    SerializableTests.serializable(
      Stateful[ReaderWriterStateT[Option, Boolean, Int, String, *], String])
  )

  checkAll(
    "ReaderWriterStateT[Option, Boolean, Int, String, Int]",
    CensorTests[ReaderWriterStateT[Option, Boolean, Int, String, *], Int].censor[Int, String]
  )
  checkAll(
    "Censor[ReaderWriterStateT[Option, Boolean, Int, String, *]]",
    SerializableTests.serializable(
      Censor[ReaderWriterStateT[Option, Boolean, Int, String, *], Int])
  )

  checkAll(
    "ReaderWriterStateT[Option, Boolean, Int, String, Boolean]",
    LocalTests[ReaderWriterStateT[Option, Boolean, Int, String, *], Boolean]
      .local[Boolean, String]
  )
  checkAll(
    "Local[ReaderWriterStateT[Option, Boolean, Int, String, *]]",
    SerializableTests.serializable(
      Local[ReaderWriterStateT[Option, Boolean, Int, String, *], Boolean])
  )

}
