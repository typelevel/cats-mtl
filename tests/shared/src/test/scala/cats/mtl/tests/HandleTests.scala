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

import cats.data.{EitherT, Kleisli, WriterT}
import cats.laws.discipline.arbitrary._
import cats.mtl.syntax.all._
import cats.syntax.all._

import org.scalacheck.{Arbitrary, Cogen}, Arbitrary.arbitrary

class HandleTests extends BaseSuite {
  type F[A] = EitherT[Eval, Throwable, A]

  test("handleForApplicativeError") {
    case class Foo[A](bar: A)

    implicit def fooApplicativeError: ApplicativeError[Foo, String] =
      new ApplicativeError[Foo, String] {
        def ap[A, B](ff: Foo[A => B])(fa: Foo[A]): Foo[B] = ???

        def pure[A](x: A): Foo[A] = ???

        def raiseError[A](e: String): Foo[A] = ???

        def handleErrorWith[A](fa: Foo[A])(f: String => Foo[A]): Foo[A] = ???
      }

    Handle[Foo, String]
    Handle[Kleisli[Foo, Unit, *], String]
    Handle[WriterT[Foo, String, *], String]
  }

  test("submerge custom errors") {
    sealed trait Error extends Product with Serializable

    object Error {
      case object First extends Error
      case object Second extends Error
      case object Third extends Error
    }

    val test =
      Handle.ensure[F, Error](implicit h => Error.Second.raise.as("nope")) recover {
        case Error.First => "0".pure[F]
        case Error.Second => "1".pure[F]
        case Error.Third => "2".pure[F]
      }

    assert(test.value.value.toOption == Some("1"))
  }

  test("submerge two independent errors") {
    sealed trait Error1 extends Product with Serializable

    object Error1 {
      case object First extends Error1
      case object Second extends Error1
      case object Third extends Error1
    }

    sealed trait Error2 extends Product with Serializable

    val test = Handle.ensure[F, Error1] { implicit h1 =>
      Handle.ensure[F, Error2] { implicit h2 =>
        // it's helpful to test the raise syntax infers even when multiple handles are present
        val _ = h2 
        Error1.Third.raise.as("nope")
      } recover { e => e.toString.pure[F] }
    } recover {
      case Error1.First => "first1".pure[F]
      case Error1.Second => "second1".pure[F]
      case Error1.Third => "third1".pure[F]
    }

    assert(test.value.value.toOption == Some("third1"))
  }

  {
    final case class Error(value: Int)

    object Error {
      implicit val arbError: Arbitrary[Error] =
        Arbitrary(arbitrary[Int].flatMap(Error(_)))

      implicit val cogenError: Cogen[Error] =
        Cogen((_: Error).value.toLong)

      implicit val eqError: Eq[Error] =
        Eq.by((_: Error).value)
    }

    implicit val eqThrowable: Eq[Throwable] =
      Eq.fromUniversalEquals[Throwable]

    val test = Handle.ensure[F, Error] { implicit h =>
      EitherT liftF {
        Eval later {
          checkAll(
            "Handle.ensure[F, Error]",
            cats.mtl.laws.discipline.HandleTests[F, Error].handle[Int])
        }
      }
    } recover { case Error(_) => ().pure[F] }

    test.value.value
    ()
  }
}
