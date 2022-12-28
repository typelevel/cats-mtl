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
import cats.syntax.all._
import org.scalacheck.Arbitrary.arbitrary
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

  private implicit def stringToStringWrapper[F[_]](
      implicit L: Local[F, String]): Local[F, StringWrapper] =
    L.imap(StringWrapper(_))(_.value)

  private implicit def stringWrapperToString[F[_]](
      implicit L: Local[F, StringWrapper]): Local[F, String] =
    L.imap(_.value)(StringWrapper(_))

  {
    Applicative[Reader[String, *]]
    checkAll(
      "Reader[String, *]",
      LocalTests[Kleisli[Id, String, *], String].local[String, String])
    checkAll(
      "Local[Kleisli[Id, String, *], String]",
      SerializableTests.serializable(Local[Kleisli[Id, String, *], String]))

    checkAll(
      "Local[Reader[String, *], String].imap",
      LocalTests[Reader[String, *], StringWrapper].local[String, String]
    )
    checkAll(
      "Local[Reader[String, *], StringWrapper]",
      SerializableTests.serializable(Local[Reader[String, *], StringWrapper]))
  }

  {
    checkAll(
      "ReaderT[Option, String, *]",
      LocalTests[ReaderTC[Option, String]#l, String].local[String, String])
    checkAll(
      "Local[ReaderTC[Option, String]#l, String]",
      SerializableTests.serializable(Local[ReaderTC[Option, String]#l, String]))

    checkAll(
      "LocalReaderT[Option, String, *].imap",
      LocalTests[ReaderTC[Option, String]#l, StringWrapper].local[String, String]
    )
    checkAll(
      "Local[ReaderTC[Option, String]#l, StringWrapper]",
      SerializableTests.serializable(Local[ReaderTC[Option, String]#l, StringWrapper]))
  }

  {

    {
      checkAll(
        "ReaderT[ReaderT[Option, String, *], Int, *]",
        LocalTests[ReaderTIntOverReaderTStringOverOption, String].local[String, String])
      checkAll(
        "Local[ReaderTIntOverReaderTStringOverOption, String]",
        SerializableTests.serializable(Local[ReaderTIntOverReaderTStringOverOption, String])
      )

      checkAll(
        "ReaderT[ReaderT[Option, StringWrapper, *], Int, String].imap",
        LocalTests[ReaderT[ReaderT[Option, StringWrapper, *], Int, *], String]
          .local[String, String])
      checkAll(
        "Local[ReaderT[ReaderT[Option, StringWrapper, *], Int, *], StringWrapper]",
        SerializableTests.serializable(
          Local[ReaderT[ReaderT[Option, StringWrapper, *], Int, *], String])
      )

      checkAll(
        "StateT[ReaderT[Option, String, *], Int, *]",
        LocalTests[StateTIntOverReaderTStringOverOption, String].local[String, String])
      checkAll(
        "Local[StateT[ReaderT[Option, String, *], Int, *], String]",
        SerializableTests.serializable(Local[StateTIntOverReaderTStringOverOption, String])
      )

      checkAll(
        "StateT[ReaderT[Option, String, *], Int, *].imap",
        LocalTests[StateT[ReaderT[Option, StringWrapper, *], Int, *], String]
          .local[String, String])
      checkAll(
        "Local[StateT[ReaderT[Option, StringWrapper, *], Int, *], String].imap",
        SerializableTests.serializable(
          Local[StateT[ReaderT[Option, StringWrapper, *], Int, *], String])
      )
    }

    checkAll(
      "WriterT[ReaderT[Option, String, *], Int, *]",
      LocalTests[WriterTIntOverReaderTStringOverOption, String].local[String, String])
    checkAll(
      "Local[WriterTIntOverReaderTStringOverOption, String]",
      SerializableTests.serializable(Local[WriterTIntOverReaderTStringOverOption, String])
    )

    checkAll(
      "WriterT[ReaderT[Option, String, *], Int, *].imap",
      LocalTests[WriterT[ReaderT[Option, StringWrapper, *], Int, *], String]
        .local[String, String])
    checkAll(
      "Local[WriterTIntOverReaderTStringOverOption, StringWrapper].imap",
      SerializableTests.serializable(
        Local[WriterT[ReaderT[Option, StringWrapper, *], Int, *], String])
    )

    checkAll(
      "OptionT[ReaderT[Option, String, *], *]",
      LocalTests[OptionTOverReaderTStringOverOption, String].local[String, String])
    checkAll(
      "Local[OptionTOverReaderTStringOverOption, String]",
      SerializableTests.serializable(Local[OptionTOverReaderTStringOverOption, String])
    )

    checkAll(
      "OptionT[ReaderT[Option, StringWrapper, *], *]",
      LocalTests[OptionT[ReaderT[Option, StringWrapper, *], *], String].local[String, String])
    checkAll(
      "Local[OptionT[ReaderT[Option, StringWrapper, *], *], String]",
      SerializableTests.serializable(
        Local[OptionT[ReaderT[Option, StringWrapper, *], *], String])
    )

    checkAll(
      "EitherT[ReaderT[Option, String, *], String, *]",
      LocalTests[EitherTIntOverReaderTStringOverOption, String].local[String, String])
    checkAll(
      "Local[EitherTIntOverReaderTStringOverOption, String]",
      SerializableTests.serializable(Local[EitherTIntOverReaderTStringOverOption, String])
    )

    checkAll(
      "EitherT[ReaderT[Option, StringWrapper, *], String, *]",
      LocalTests[EitherT[ReaderT[Option, StringWrapper, *], String, *], String]
        .local[String, String])
    checkAll(
      "Local[EitherT[ReaderT[Option, StringWrapper, *], String, *], String]",
      SerializableTests.serializable(
        Local[EitherT[ReaderT[Option, StringWrapper, *], String, *], String])
    )

  }

}

/**
 * Only exists as a type that is trivially isomorphic to `String`
 */
case class StringWrapper(value: String)
object StringWrapper {
  implicit val stringWrapperEq: Eq[StringWrapper] = Eq.by(_.value)
  implicit val arbStringWrapper: Arbitrary[StringWrapper] = Arbitrary(
    arbitrary[String].map(StringWrapper(_)))
  implicit val cogenStringWrapper: Cogen[StringWrapper] = Cogen[String].contramap(_.value)
}
