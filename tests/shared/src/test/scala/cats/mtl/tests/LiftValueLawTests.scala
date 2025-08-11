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

package cats
package mtl
package tests

import cats.data.{ReaderWriterStateT => RWST, _}
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline.LiftValueTests
import org.scalacheck.Arbitrary

class LiftValueLawTests extends BaseSuite {
  implicit def eqKleisli[F[_], A, B](
      implicit arbA: Arbitrary[A],
      eqFB: Eq[F[B]]
  ): Eq[Kleisli[F, A, B]] =
    Eq.by(_.run)

  implicit def stateTEq[F[_]: FlatMap, S, A](
      implicit arbS: Arbitrary[S],
      eqFSA: Eq[F[(S, A)]]
  ): Eq[StateT[F, S, A]] =
    Eq.by(state => (s: S) => state.run(s))

  implicit def rwstEq[F[_]: Monad, E, L, S, A](
      implicit arbE: Arbitrary[E],
      arbS: Arbitrary[S],
      eqFLSA: Eq[F[(L, S, A)]]
  ): Eq[RWST[F, E, L, S, A]] =
    Eq.by(rwst => (e: E, s: S) => rwst.run(e, s))

  // identity
  checkAll("LiftValue[List, List]", LiftValueTests[List, List].liftValue[String, Int])

  // non-compositional
  checkAll(
    "LiftValue[List, EitherT[List, Int, *]]",
    LiftValueTests[List, EitherT[List, Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, IorT[List, Int, *]]",
    LiftValueTests[List, IorT[List, Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, Kleisli[List, Int, *]]",
    LiftValueTests[List, Kleisli[List, Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, OptionT[List, *]]",
    LiftValueTests[List, OptionT[List, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, RWST[List, Int, Int, Int, *]]",
    LiftValueTests[List, RWST[List, Int, Int, Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, StateT[List, Int, *]]",
    LiftValueTests[List, StateT[List, Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, WriterT[List, Int, *]]",
    LiftValueTests[List, WriterT[List, Int, *]].liftValue[String, Int]
  )

  // compositional
  checkAll(
    "LiftValue[List, EitherT[OptionT[List, *], Int, *]]",
    LiftValueTests[List, EitherT[OptionT[List, *], Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, IorT[OptionT[List, *], Int, *]]",
    LiftValueTests[List, IorT[OptionT[List, *], Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, Kleisli[OptionT[List, *], Int, *]]",
    LiftValueTests[List, Kleisli[OptionT[List, *], Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, OptionT[IorT[List, Int, *], *]]",
    LiftValueTests[List, OptionT[IorT[List, Int, *], *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, RWST[OptionT[List, *], Int, Int, Int, *]]",
    LiftValueTests[List, RWST[OptionT[List, *], Int, Int, Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, StateT[OptionT[List, *], Int, *]]",
    LiftValueTests[List, StateT[OptionT[List, *], Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftValue[List, WriterT[OptionT[List, *], Int, *]]",
    LiftValueTests[List, WriterT[OptionT[List, *], Int, *]].liftValue[String, Int]
  )
}
