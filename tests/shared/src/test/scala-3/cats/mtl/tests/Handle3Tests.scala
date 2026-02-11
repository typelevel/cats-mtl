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

import cats.data.EitherT
import cats.mtl.syntax.all.*
import cats.syntax.all.*
import cats.mtl.Handle.*

class Handle3Tests extends munit.FunSuite:

  type F[A] = EitherT[Eval, Throwable, A]

  test("submerge custom errors (scala 3)"):
    enum Error:
      case First, Second, Third

    val test =
      allow[Error]:
        Error.Second.raise[F, String].as("nope")
      .rescue:
        case Error.First => "0".pure[F]
        case Error.Second => "1".pure[F]
        case Error.Third => "2".pure[F]

    assert.equals(test.value.value.toOption, Some("1"))

  test("submerge two independent errors (scala 3)"):
    enum Error1:
      case First, Second, Third
    enum Error2:
      case Fourth
    val test =
      allow[Error1]:
        allow[Error2]:
          Error1.Third.raise[F, String].as("nope")
        .rescue:
          case e => e.toString.pure[F]
      .rescue:
        case Error1.First => "first1".pure[F]
        case Error1.Second => "second1".pure[F]
        case Error1.Third => "third1".pure[F]
    assert.equals(test.value.value.toOption, Some("third1"))

  test("submerge two independent errors with union(scala 3)"):
    enum Error1:
      case First, Second, Third
    enum Error2:
      case Fourth
    val test =
      allow[Error1 | Error2]:
        Error1.Third.raise[F, String].as("nope")
      .rescue:
        case Error1.First => "first1".pure[F]
        case Error1.Second => "second1".pure[F]
        case Error1.Third => "third1".pure[F]
        case Error2.Fourth => "fourth1".pure[F]
    assert.equals(test.value.value.toOption, Some("third1"))

  test("attempt - return Either[E, A]"):
    enum Error:
      case First, Second, Third

    val success: F[Either[Error, String]] =
      allow[Error]:
        EitherT.rightT[Eval, Throwable]("all good")
      .attempt

    val failure =
      allow[Error]:
        Error.Second.raise[F, String].as("nope")
      .attempt

    assert(success.value.value == Right(Right("all good")))
    assert(failure.value.value == Right(Left(Error.Second)))

  test("attempt - propagate unhandled exceptions"):
    enum Error:
      case First, Second, Third

    val exception = new RuntimeException("oops")

    val test =
      allow[Error]:
        EitherT.leftT[Eval, Unit](exception)
      .attempt

    assert(test.value.value == Left(exception))
