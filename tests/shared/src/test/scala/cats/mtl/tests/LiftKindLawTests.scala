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

import cats.data._
import cats.laws.discipline.SerializableTests
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline.LiftKindTests
import cats.mtl.laws.discipline.LocalTests
import org.scalacheck.Arbitrary

class LiftKindLawTests extends BaseSuite {
  import LiftKindTests.arbitraryFunctionKListList

  implicit def eqKleisli[F[_], A, B](
      implicit arbA: Arbitrary[A],
      eqFB: Eq[F[B]]
  ): Eq[Kleisli[F, A, B]] =
    Eq.by(_.run)

  // identity
  checkAll("LiftKind[List, List]", LiftKindTests[List, List].liftKind[String, Int])

  // non-compositional
  checkAll(
    "LiftKind[List, EitherT[List, Int, *]]",
    LiftKindTests[List, EitherT[List, Int, *]].liftKind[String, Int]
  )
  checkAll(
    "LiftKind[List, IorT[List, Int, *]]",
    LiftKindTests[List, IorT[List, Int, *]].liftKind[String, Int]
  )
  checkAll(
    "LiftKind[List, Kleisli[List, Int, *]]",
    LiftKindTests[List, Kleisli[List, Int, *]].liftKind[String, Int]
  )
  checkAll(
    "LiftKind[List, OptionT[List, *]]",
    LiftKindTests[List, OptionT[List, *]].liftKind[String, Int]
  )
  checkAll(
    "LiftKind[List, WriterT[List, Int, *]]",
    LiftKindTests[List, WriterT[List, Int, *]].liftKind[String, Int]
  )

  // compositional
  checkAll(
    "LiftKind[List, EitherT[OptionT[List, *], Int, *]]",
    LiftKindTests[List, EitherT[OptionT[List, *], Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftKind[List, IorT[OptionT[List, *], Int, *]]",
    LiftKindTests[List, IorT[OptionT[List, *], Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftKind[List, Kleisli[OptionT[List, *], Int, *]]",
    LiftKindTests[List, Kleisli[OptionT[List, *], Int, *]].liftValue[String, Int]
  )
  checkAll(
    "LiftKind[List, OptionT[IorT[List, Int, *], *]]",
    LiftKindTests[List, OptionT[IorT[List, Int, *], *]].liftValue[String, Int]
  )
  checkAll(
    "LiftKind[List, WriterT[OptionT[List, *], Int, *]]",
    LiftKindTests[List, WriterT[OptionT[List, *], Int, *]].liftValue[String, Int]
  )

  // lift Local
  locally {
    implicit val liftedLocal: Local[OptionT[IorT[Reader[Int, *], Int, *], *], Int] =
      Local[Reader[Int, *], Int].liftTo[OptionT[IorT[Reader[Int, *], Int, *], *]]
    checkAll(
      "Local[OptionT[IorT[Reader[Int, *], Int, *], *], *], Int]",
      LocalTests[OptionT[IorT[Reader[Int, *], Int, *], *], Int].local[String, Int]
    )
    SerializableTests.serializable(Local[OptionT[IorT[Reader[Int, *], Int, *], *], Int])
  }
  locally {
    implicit val liftedLocal: Local[OptionT[IorT[Reader[Int, *], Int, *], *], Int] =
      Local[Reader[Int, *], Int]
        .liftTo[IorT[Reader[Int, *], Int, *]]
        .liftTo[OptionT[IorT[Reader[Int, *], Int, *], *]]
    checkAll(
      "Local[OptionT[IorT[Reader[Int, *], Int, *], *], *], Int]",
      LocalTests[OptionT[IorT[Reader[Int, *], Int, *], *], Int].local[String, Int]
    )
    SerializableTests.serializable(Local[OptionT[IorT[Reader[Int, *], Int, *], *], Int])
  }
}
