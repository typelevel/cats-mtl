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

import cats.arrow.FunctionK
import cats.data._
import cats.laws.discipline.SerializableTests
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.eq._
import cats.mtl.laws.discipline._
import org.scalacheck._

class ReaderTTests extends BaseSuite {
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

  implicit def eqKleisliId[A, B](implicit arb: Arbitrary[A], ev: Eq[B]): Eq[Kleisli[Id, A, B]] =
    eqKleisli[Id, A, B]

  implicit def stateTEq[F[_], S, A](
      implicit S: Arbitrary[S],
      FSA: Eq[F[(S, A)]],
      F: FlatMap[F]): Eq[StateT[F, S, A]] =
    Eq.by[StateT[F, S, A], S => F[(S, A)]](state => s => state.run(s))

  {
    Applicative[Reader[String, *]]
    checkAll(
      "Reader[String, *]",
      LocalTests[Kleisli[Id, String, *], String].local[String, String])
    checkAll(
      "FunctorLocal[Reader[String, *], String]",
      SerializableTests.serializable(Local[Kleisli[Id, String, *], String]))
  }

  {
    checkAll(
      "ReaderT[Option, String, *]",
      LocalTests[ReaderTC[Option, String]#l, String].local[String, String])
    checkAll(
      "FunctorLocal[ReaderT[Option, String, *], String]",
      SerializableTests.serializable(Local[ReaderTC[Option, String]#l, String]))
  }

  {

    {
      checkAll(
        "ReaderT[ReaderT[Option, String, *], Int, *]",
        LocalTests[ReaderTIntOverReaderTStringOverOption, String].local[String, String])
      checkAll(
        "FunctorLocal[ReaderT[ReaderT[Option, String, *], Int, *], String]",
        SerializableTests.serializable(Local[ReaderTIntOverReaderTStringOverOption, String])
      )

      checkAll(
        "StateT[ReaderT[Option, String, *], Int, *]",
        LocalTests[StateTIntOverReaderTStringOverOption, String].local[String, String])
      checkAll(
        "FunctorLocal[StateT[ReaderT[Option, String, *], Int, *], String]",
        SerializableTests.serializable(Local[StateTIntOverReaderTStringOverOption, String])
      )
    }

    checkAll(
      "WriterT[ReaderT[Option, String, *], Int, *]",
      LocalTests[WriterTIntOverReaderTStringOverOption, String].local[String, String])
    checkAll(
      "FunctorLocal[WriterT[ReaderT[Option, String, *], Int, *], String]",
      SerializableTests.serializable(Local[WriterTIntOverReaderTStringOverOption, String])
    )

    checkAll(
      "OptionT[ReaderT[Option, String, *], *]",
      LocalTests[OptionTOverReaderTStringOverOption, String].local[String, String])
    checkAll(
      "FunctorLocal[OptionT[ReaderT[Option, String, *], *], String]",
      SerializableTests.serializable(Local[OptionTOverReaderTStringOverOption, String])
    )

    checkAll(
      "EitherT[ReaderT[Option, String, *], String, *]",
      LocalTests[EitherTIntOverReaderTStringOverOption, String].local[String, String])
    checkAll(
      "FunctorLocal[EitherT[ReaderT[Option, String, *], Int, *], String]",
      SerializableTests.serializable(Local[EitherTIntOverReaderTStringOverOption, String])
    )

  }

}
