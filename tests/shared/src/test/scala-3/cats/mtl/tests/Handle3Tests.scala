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

class Handle3Tests extends BaseSuite:

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

    assert(test.value.value.toOption == Some("1"))

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
    assert(test.value.value.toOption == Some("third1"))
