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

import cats._
import cats.arrow.FunctionK
import cats.data._
import cats.laws.discipline.{ExhaustiveCheck, SerializableTests}
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline._
import org.scalacheck._

class WriterTTests extends BaseSuite {
  implicit val arbFunctionK: Arbitrary[Option ~> Option] =
    Arbitrary(
      Gen.oneOf(
        new (Option ~> Option) {
          def apply[A](fa: Option[A]): Option[A] = None
        },
        FunctionK.id[Option]))

  implicit def eqKleisli[F[_], A, B](
      implicit arb: ExhaustiveCheck[A],
      ev: Eq[F[B]]): Eq[Kleisli[F, A, B]] =
    Eq.by((x: Kleisli[F, A, B]) => x.run)

  implicit def stateTEq[F[_], S, A](
      implicit S: ExhaustiveCheck[S],
      FSA: Eq[F[(S, A)]],
      F: FlatMap[F]): Eq[StateT[F, S, A]] =
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state => s => state.run(s))

  type WriterTStringOverWriterTStringOverOption[A] =
    WriterT[WriterTC[Option, String]#l, List[Int], A]

  {
    Tell[Writer[Chain[String], *], Chain[String]]
    Listen[Writer[Chain[String], *], Chain[String]]
  }

  {
    import cats.laws.discipline.arbitrary._

    checkAll(
      "WriterT[WriterTC[Option, String]#l, List[Int], String]",
      CensorTests[WriterTStringOverWriterTStringOverOption, String].censor[String, String]
    )
    checkAll(
      "ApplicativePass[WriterT[WriterTC[Option, String]#l, List[Int], String]",
      SerializableTests.serializable(Censor[WriterTStringOverWriterTStringOverOption, String])
    )

    checkAll(
      "ReaderT[WriterTC[Option, String]#l, List[Int], String]",
      CensorTests[ReaderTStringOverWriterTStringOverOption, String].censor[String, String]
    )
    checkAll(
      "ApplicativePass[ReaderT[WriterTC[Option, String]#l, List[Int], String]",
      SerializableTests.serializable(Censor[ReaderTStringOverWriterTStringOverOption, String])
    )

    checkAll(
      "StateT[WriterTC[Option, String]#l, List[Int], String]",
      CensorTests[StateTStringOverWriterTStringOverOption, String].censor[String, String]
    )
    checkAll(
      "ApplicativePass[StateT[WriterTC[Option, String]#l, List[Int], String]",
      SerializableTests.serializable(Censor[StateTStringOverWriterTStringOverOption, String])
    )
  }

  {
    import cats.laws.discipline.arbitrary._

    checkAll(
      "WriterT[Option, String, String]",
      CensorTests[WriterTC[Option, String]#l, String].censor[String, String])
    checkAll(
      "Listen[WriterT[Option, String, *]]",
      SerializableTests.serializable(Listen[WriterTC[Option, String]#l, String]))
  }
}
